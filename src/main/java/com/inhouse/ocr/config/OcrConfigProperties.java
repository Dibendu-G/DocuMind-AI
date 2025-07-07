package com.inhouse.ocr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ocr")
public class OcrConfigProperties {
    
    private Processing processing = new Processing();
    private Models models = new Models();
    private Image image = new Image();
    private Performance performance = new Performance();
    
    public Processing getProcessing() {
        return processing;
    }
    
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }
    
    public Models getModels() {
        return models;
    }
    
    public void setModels(Models models) {
        this.models = models;
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    public Performance getPerformance() {
        return performance;
    }
    
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
    
    public static class Processing {
        private String tempDirectory;
        private int maxConcurrentJobs;
        private int timeoutMinutes;
        
        public String getTempDirectory() {
            return tempDirectory;
        }
        
        public void setTempDirectory(String tempDirectory) {
            this.tempDirectory = tempDirectory;
        }
        
        public int getMaxConcurrentJobs() {
            return maxConcurrentJobs;
        }
        
        public void setMaxConcurrentJobs(int maxConcurrentJobs) {
            this.maxConcurrentJobs = maxConcurrentJobs;
        }
        
        public int getTimeoutMinutes() {
            return timeoutMinutes;
        }
        
        public void setTimeoutMinutes(int timeoutMinutes) {
            this.timeoutMinutes = timeoutMinutes;
        }
    }
    
    public static class Models {
        private String basePath;
        private String textDetection;
        private String textRecognition;
        private String imageEnhancement;
        
        public String getBasePath() {
            return basePath;
        }
        
        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
        
        public String getTextDetection() {
            return textDetection;
        }
        
        public void setTextDetection(String textDetection) {
            this.textDetection = textDetection;
        }
        
        public String getTextRecognition() {
            return textRecognition;
        }
        
        public void setTextRecognition(String textRecognition) {
            this.textRecognition = textRecognition;
        }
        
        public String getImageEnhancement() {
            return imageEnhancement;
        }
        
        public void setImageEnhancement(String imageEnhancement) {
            this.imageEnhancement = imageEnhancement;
        }
    }
    
    public static class Image {
        private int maxWidth;
        private int maxHeight;
        private double qualityThreshold;
        
        public int getMaxWidth() {
            return maxWidth;
        }
        
        public void setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }
        
        public int getMaxHeight() {
            return maxHeight;
        }
        
        public void setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
        }
        
        public double getQualityThreshold() {
            return qualityThreshold;
        }
        
        public void setQualityThreshold(double qualityThreshold) {
            this.qualityThreshold = qualityThreshold;
        }
    }
    
    public static class Performance {
        private int batchSize;
        private boolean cacheEnabled;
        private int cacheMaxSize;
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public boolean isCacheEnabled() {
            return cacheEnabled;
        }
        
        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }
        
        public int getCacheMaxSize() {
            return cacheMaxSize;
        }
        
        public void setCacheMaxSize(int cacheMaxSize) {
            this.cacheMaxSize = cacheMaxSize;
        }
    }
}
