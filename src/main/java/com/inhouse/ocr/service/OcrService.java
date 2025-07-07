package com.inhouse.ocr.service;

import com.inhouse.ocr.config.OcrConfigProperties;
import com.inhouse.ocr.dto.OcrResponseDto;
import com.inhouse.ocr.entity.OcrJob;
import com.inhouse.ocr.repository.OcrJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OcrService {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    
    @Autowired
    private OcrJobRepository jobRepository;
    
    @Autowired
    private PdfProcessingService pdfProcessingService;
    
    @Autowired
    private ImagePreprocessingService imagePreprocessingService;
    
    @Autowired
    private OcrConfigProperties config;
    
    // In-memory cache for job results (will be replaced with proper caching in production)
    private final Map<String, List<OcrResponseDto.PageResult>> detailedResultsCache = new ConcurrentHashMap<>();
    
    /**
     * Process a single OCR job
     */
    public String processOcrJob(MultipartFile file) throws IOException {
        String jobId = generateJobId();
        String originalFilename = file.getOriginalFilename();
        
        // Save uploaded file temporarily
        String tempDir = config.getProcessing().getTempDirectory();
        ensureDirectoryExists(tempDir);
        
        String savedFilePath = saveUploadedFile(file, tempDir, jobId);
        
        // Create job record
        OcrJob job = new OcrJob(jobId, originalFilename, savedFilePath);
        job = jobRepository.save(job);
        
        // Start async processing
        processOcrJobAsync(job);
        
        return jobId;
    }
    
    /**
     * Process batch of OCR jobs
     */
    public List<String> processBatchOcrJobs(List<MultipartFile> files) throws IOException {
        List<String> jobIds = new ArrayList<>();
        
        for (MultipartFile file : files) {
            String jobId = processOcrJob(file);
            jobIds.add(jobId);
        }
        
        return jobIds;
    }
    
    /**
     * Get OCR job by ID
     */
    public OcrJob getOcrJob(String jobId) {
        Optional<OcrJob> job = jobRepository.findByJobId(jobId);
        return job.orElse(null);
    }
    
    /**
     * Cancel OCR job
     */
    public boolean cancelOcrJob(String jobId) {
        Optional<OcrJob> jobOpt = jobRepository.findByJobId(jobId);
        
        if (jobOpt.isPresent()) {
            OcrJob job = jobOpt.get();
            if (job.getStatus() == OcrJob.JobStatus.PENDING || 
                job.getStatus() == OcrJob.JobStatus.PROCESSING) {
                
                job.setStatus(OcrJob.JobStatus.CANCELLED);
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);
                
                // Clean up temp files
                cleanupJobFiles(job);
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get detailed results for a job
     */
    public List<OcrResponseDto.PageResult> getDetailedResults(String jobId) {
        return detailedResultsCache.get(jobId);
    }
    
    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalJobs", jobRepository.count());
        stats.put("pendingJobs", jobRepository.countByStatus(OcrJob.JobStatus.PENDING));
        stats.put("processingJobs", jobRepository.countByStatus(OcrJob.JobStatus.PROCESSING));
        stats.put("completedJobs", jobRepository.countByStatus(OcrJob.JobStatus.COMPLETED));
        stats.put("failedJobs", jobRepository.countByStatus(OcrJob.JobStatus.FAILED));
        
        Double avgProcessingTime = jobRepository.getAverageProcessingTime();
        stats.put("averageProcessingTimeMs", avgProcessingTime != null ? avgProcessingTime : 0);
        
        Double avgConfidence = jobRepository.getAverageConfidence();
        stats.put("averageConfidence", avgConfidence != null ? avgConfidence : 0);
        
        // Recent jobs (last 24 hours)
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<OcrJob> recentJobs = jobRepository.findRecentJobs(since);
        stats.put("recentJobs", recentJobs.size());
        
        return stats;
    }
    
    /**
     * Get health status of system components
     */
    public Map<String, String> getHealthStatus() {
        Map<String, String> health = new HashMap<>();
        
        // Check database connectivity
        try {
            jobRepository.count();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
        }
        
        // Check OpenCV
        health.put("opencv", imagePreprocessingService.isOpenCvLoaded() ? "UP" : "DOWN");
        
        // Check temp directory
        try {
            Path tempPath = Paths.get(config.getProcessing().getTempDirectory());
            health.put("tempDirectory", Files.exists(tempPath) && Files.isWritable(tempPath) ? "UP" : "DOWN");
        } catch (Exception e) {
            health.put("tempDirectory", "DOWN");
        }
        
        // Check model directory (placeholder for future model loading)
        try {
            Path modelPath = Paths.get(config.getModels().getBasePath());
            health.put("modelDirectory", Files.exists(modelPath) ? "UP" : "DOWN");
        } catch (Exception e) {
            health.put("modelDirectory", "DOWN");
        }
        
        return health;
    }
    
    /**
     * Async processing of OCR job
     */
    @Async
    public CompletableFuture<Void> processOcrJobAsync(OcrJob job) {
        try {
            logger.info("Starting OCR processing for job: {}", job.getJobId());
            
            // Update job status
            job.startProcessing();
            jobRepository.save(job);
            
            // Load PDF file
            File pdfFile = new File(job.getFilePath());
            if (!pdfFile.exists()) {
                throw new IOException("PDF file not found: " + job.getFilePath());
            }
            
            // Get page count
            int pageCount = pdfProcessingService.getPageCount(pdfFile);
            job.setTotalPages(pageCount);
            jobRepository.save(job);
            
            // Convert PDF to images
            List<BufferedImage> images = pdfProcessingService.convertPdfToImages(pdfFile);
            logger.info("Converted {} pages to images for job: {}", images.size(), job.getJobId());
            
            // Process each page
            StringBuilder allText = new StringBuilder();
            List<OcrResponseDto.PageResult> pageResults = new ArrayList<>();
            double totalConfidence = 0.0;
            
            for (int i = 0; i < images.size(); i++) {
                try {
                    logger.debug("Processing page {} of {}", i + 1, images.size());
                    
                    // Preprocess image
                    BufferedImage preprocessedImage = imagePreprocessingService.preprocessImage(images.get(i));
                    
                    // Assess image quality
                    double quality = imagePreprocessingService.assessImageQuality(preprocessedImage);
                    
                    // Perform OCR (placeholder - will be replaced with neural network)
                    String pageText = performBasicOcr(preprocessedImage);
                    double confidence = calculateConfidence(quality, pageText);
                    
                    // Create page result
                    OcrResponseDto.PageResult pageResult = new OcrResponseDto.PageResult(i + 1, pageText, confidence);
                    pageResults.add(pageResult);
                    
                    // Accumulate text and confidence
                    allText.append(pageText).append("\n\n");
                    totalConfidence += confidence;
                    
                    // Update progress
                    job.setProcessedPages(i + 1);
                    jobRepository.save(job);
                    
                } catch (Exception e) {
                    logger.error("Error processing page {} for job: {}", i + 1, job.getJobId(), e);
                    // Continue with next page
                }
            }
            
            // Calculate overall confidence
            double overallConfidence = pageResults.isEmpty() ? 0.0 : totalConfidence / pageResults.size();
            
            // Update job with results
            job.setExtractedText(allText.toString().trim());
            job.setOverallConfidence(overallConfidence);
            job.completeProcessing();
            jobRepository.save(job);
            
            // Cache detailed results
            detailedResultsCache.put(job.getJobId(), pageResults);
            
            logger.info("OCR processing completed for job: {} with confidence: {:.2f}%", 
                       job.getJobId(), overallConfidence * 100);
            
        } catch (Exception e) {
            logger.error("OCR processing failed for job: " + job.getJobId(), e);
            job.failProcessing("Processing failed: " + e.getMessage());
            jobRepository.save(job);
        } finally {
            // Clean up temporary files
            cleanupJobFiles(job);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Placeholder OCR implementation
     * This will be replaced with neural network-based OCR in later phases
     */
    private String performBasicOcr(BufferedImage image) {
        // Placeholder implementation - returns sample text based on image characteristics
        int width = image.getWidth();
        int height = image.getHeight();
        int pixels = width * height;
        
        // Simple placeholder that generates text based on image properties
        StringBuilder text = new StringBuilder();
        text.append("OCR processing completed for image (").append(width).append("x").append(height).append(")\n");
        text.append("This is a placeholder implementation.\n");
        text.append("Neural network-based OCR will be implemented in Phase 3.\n");
        
        if (pixels > 1000000) {
            text.append("High resolution image detected - likely printed text.\n");
        } else {
            text.append("Standard resolution image - may contain handwritten content.\n");
        }
        
        text.append("Sample extracted text would appear here.\n");
        text.append("Confidence scoring and detailed analysis pending neural network implementation.");
        
        return text.toString();
    }
    
    /**
     * Calculate confidence score based on image quality and text characteristics
     */
    private double calculateConfidence(double imageQuality, String extractedText) {
        // Basic confidence calculation
        double baseConfidence = 0.7; // Placeholder base confidence
        
        // Adjust based on image quality
        double qualityBonus = imageQuality * 0.2;
        
        // Adjust based on text length (longer text generally means better extraction)
        double textLengthBonus = Math.min(extractedText.length() / 1000.0 * 0.1, 0.1);
        
        return Math.min(baseConfidence + qualityBonus + textLengthBonus, 1.0);
    }
    
    /**
     * Generate unique job ID
     */
    private String generateJobId() {
        return "OCR_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Save uploaded file to temporary directory
     */
    private String saveUploadedFile(MultipartFile file, String tempDir, String jobId) throws IOException {
        String filename = jobId + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(tempDir, filename);
        
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    /**
     * Ensure directory exists
     */
    private void ensureDirectoryExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
    
    /**
     * Clean up temporary files for a job
     */
    private void cleanupJobFiles(OcrJob job) {
        try {
            // Delete original PDF file
            if (job.getFilePath() != null) {
                File pdfFile = new File(job.getFilePath());
                if (pdfFile.exists()) {
                    pdfFile.delete();
                    logger.debug("Cleaned up PDF file: {}", job.getFilePath());
                }
            }
            
            // Delete any temporary image files
            String tempDir = config.getProcessing().getTempDirectory();
            Path tempDirPath = Paths.get(tempDir);
            
            if (Files.exists(tempDirPath)) {
                Files.list(tempDirPath)
                    .filter(path -> path.getFileName().toString().startsWith(job.getJobId()))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.debug("Cleaned up temp file: {}", path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete temp file: {}", path, e);
                        }
                    });
            }
            
        } catch (Exception e) {
            logger.warn("Error during cleanup for job: {}", job.getJobId(), e);
        }
    }
}
