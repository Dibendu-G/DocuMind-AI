package com.inhouse.ocr.dto;

import com.inhouse.ocr.entity.OcrJob;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OcrResponseDto {
    
    private String jobId;
    private String originalFilename;
    private String status;
    private String errorMessage;
    private Integer totalPages;
    private Integer processedPages;
    private Double overallConfidence;
    private String extractedText;
    private Long processingTimeMs;
    private Double progressPercentage;
    private List<PageResult> pageResults;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    public OcrResponseDto() {}
    
    public OcrResponseDto(OcrJob job) {
        this.jobId = job.getJobId();
        this.originalFilename = job.getOriginalFilename();
        this.status = job.getStatus().name();
        this.errorMessage = job.getErrorMessage();
        this.totalPages = job.getTotalPages();
        this.processedPages = job.getProcessedPages();
        this.overallConfidence = job.getOverallConfidence();
        this.extractedText = job.getExtractedText();
        this.processingTimeMs = job.getProcessingTimeMs();
        this.createdAt = job.getCreatedAt();
        this.startedAt = job.getStartedAt();
        this.completedAt = job.getCompletedAt();
        
        if (job.getTotalPages() != null && job.getTotalPages() > 0) {
            this.progressPercentage = (double) job.getProcessedPages() / job.getTotalPages() * 100;
        }
    }
    
    // Getters and Setters
    public String getJobId() {
        return jobId;
    }
    
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getProcessedPages() {
        return processedPages;
    }
    
    public void setProcessedPages(Integer processedPages) {
        this.processedPages = processedPages;
    }
    
    public Double getOverallConfidence() {
        return overallConfidence;
    }
    
    public void setOverallConfidence(Double overallConfidence) {
        this.overallConfidence = overallConfidence;
    }
    
    public String getExtractedText() {
        return extractedText;
    }
    
    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public List<PageResult> getPageResults() {
        return pageResults;
    }
    
    public void setPageResults(List<PageResult> pageResults) {
        this.pageResults = pageResults;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public static class PageResult {
        private int pageNumber;
        private String extractedText;
        private double confidence;
        private List<TextRegion> textRegions;
        
        public PageResult() {}
        
        public PageResult(int pageNumber, String extractedText, double confidence) {
            this.pageNumber = pageNumber;
            this.extractedText = extractedText;
            this.confidence = confidence;
        }
        
        // Getters and Setters
        public int getPageNumber() {
            return pageNumber;
        }
        
        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }
        
        public String getExtractedText() {
            return extractedText;
        }
        
        public void setExtractedText(String extractedText) {
            this.extractedText = extractedText;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
        
        public List<TextRegion> getTextRegions() {
            return textRegions;
        }
        
        public void setTextRegions(List<TextRegion> textRegions) {
            this.textRegions = textRegions;
        }
    }
    
    public static class TextRegion {
        private String text;
        private double confidence;
        private BoundingBox boundingBox;
        
        public TextRegion() {}
        
        public TextRegion(String text, double confidence, BoundingBox boundingBox) {
            this.text = text;
            this.confidence = confidence;
            this.boundingBox = boundingBox;
        }
        
        // Getters and Setters
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
        
        public BoundingBox getBoundingBox() {
            return boundingBox;
        }
        
        public void setBoundingBox(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }
    }
    
    public static class BoundingBox {
        private int x;
        private int y;
        private int width;
        private int height;
        
        public BoundingBox() {}
        
        public BoundingBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        // Getters and Setters
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
    }
}
