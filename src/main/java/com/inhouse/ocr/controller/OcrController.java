package com.inhouse.ocr.controller;

import com.inhouse.ocr.dto.OcrResponseDto;
import com.inhouse.ocr.entity.OcrJob;
import com.inhouse.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ocr")
@CrossOrigin(origins = "*")
public class OcrController {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);
    
    @Autowired
    private OcrService ocrService;
    
    /**
     * Upload and process PDF file
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") @NotNull MultipartFile file) {
        try {
            logger.info("Received file upload request: {} ({}KB)", 
                       file.getOriginalFilename(), file.getSize() / 1024);
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("File is empty"));
            }
            
            if (!isPdfFile(file)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Only PDF files are supported"));
            }
            
            if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("File size exceeds 100MB limit"));
            }
            
            // Start OCR processing
            String jobId = ocrService.processOcrJob(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("message", "File uploaded successfully. Processing started.");
            response.put("status", "PENDING");
            
            logger.info("OCR job created with ID: {}", jobId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to process file: " + e.getMessage()));
        }
    }
    
    /**
     * Get OCR job status
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        try {
            logger.debug("Checking status for job: {}", jobId);
            
            OcrJob job = ocrService.getOcrJob(jobId);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            OcrResponseDto response = new OcrResponseDto(job);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting job status for: " + jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get job status"));
        }
    }
    
    /**
     * Get OCR results
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<?> getOcrResult(@PathVariable String jobId) {
        try {
            logger.debug("Getting results for job: {}", jobId);
            
            OcrJob job = ocrService.getOcrJob(jobId);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (job.getStatus() == OcrJob.JobStatus.PENDING || 
                job.getStatus() == OcrJob.JobStatus.PROCESSING) {
                return ResponseEntity.accepted()
                    .body(createErrorResponse("Job is still processing"));
            }
            
            if (job.getStatus() == OcrJob.JobStatus.FAILED) {
                return ResponseEntity.ok(new OcrResponseDto(job));
            }
            
            OcrResponseDto response = new OcrResponseDto(job);
            // Add detailed results if available
            response.setPageResults(ocrService.getDetailedResults(jobId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting OCR results for: " + jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get OCR results"));
        }
    }
    
    /**
     * Process batch of PDF files
     */
    @PostMapping("/batch")
    public ResponseEntity<?> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        try {
            logger.info("Received batch upload request with {} files", files.size());
            
            if (files.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("No files provided"));
            }
            
            if (files.size() > 10) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Maximum 10 files allowed per batch"));
            }
            
            // Validate all files first
            for (MultipartFile file : files) {
                if (file.isEmpty() || !isPdfFile(file)) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("All files must be valid PDFs"));
                }
            }
            
            // Process batch
            List<String> jobIds = ocrService.processBatchOcrJobs(files);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobIds", jobIds);
            response.put("message", "Batch uploaded successfully. Processing started.");
            response.put("totalJobs", jobIds.size());
            
            logger.info("Batch OCR jobs created: {}", jobIds);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing batch upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to process batch: " + e.getMessage()));
        }
    }
    
    /**
     * Get batch status
     */
    @GetMapping("/batch/status")
    public ResponseEntity<?> getBatchStatus(@RequestParam List<String> jobIds) {
        try {
            logger.debug("Getting batch status for {} jobs", jobIds.size());
            
            List<OcrResponseDto> responses = jobIds.stream()
                .map(jobId -> {
                    OcrJob job = ocrService.getOcrJob(jobId);
                    return job != null ? new OcrResponseDto(job) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("jobs", responses);
            result.put("totalJobs", responses.size());
            
            long completed = responses.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
            long failed = responses.stream()
                .filter(r -> "FAILED".equals(r.getStatus()))
                .count();
            long processing = responses.stream()
                .filter(r -> "PROCESSING".equals(r.getStatus()) || "PENDING".equals(r.getStatus()))
                .count();
            
            result.put("completed", completed);
            result.put("failed", failed);
            result.put("processing", processing);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error getting batch status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get batch status"));
        }
    }
    
    /**
     * Cancel OCR job
     */
    @DeleteMapping("/cancel/{jobId}")
    public ResponseEntity<?> cancelJob(@PathVariable String jobId) {
        try {
            logger.info("Cancelling job: {}", jobId);
            
            boolean cancelled = ocrService.cancelOcrJob(jobId);
            if (!cancelled) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Job cannot be cancelled (not found or already completed)"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("message", "Job cancelled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cancelling job: " + jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to cancel job"));
        }
    }
    
    /**
     * Get system statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = ocrService.getSystemStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting system statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get system statistics"));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("version", "1.0.0");
            
            // Check system health
            Map<String, String> components = ocrService.getHealthStatus();
            health.put("components", components);
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createErrorResponse("Service unhealthy"));
        }
    }
    
    /**
     * Validate if file is a PDF
     */
    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
    
    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
