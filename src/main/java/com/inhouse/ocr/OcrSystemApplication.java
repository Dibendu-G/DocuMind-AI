package com.inhouse.ocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DocuMind AI - Enterprise OCR Platform
 * 
 * A complete enterprise-grade OCR platform for processing PDFs with maximum accuracy
 * while maintaining complete data privacy through local processing.
 * 
 * @author DEV. DIBENDU (Lead Developer & Project Owner)
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableAsync
public class OcrSystemApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrSystemApplication.class);
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("DocuMind AI - Enterprise OCR Platform");
        logger.info("Developed by: DEV. DIBENDU");
        logger.info("Version: 1.0.0");
        logger.info("========================================");
        
        SpringApplication.run(OcrSystemApplication.class, args);
        
        logger.info("🚀 DocuMind AI Platform started successfully!");
        logger.info("📡 API Base URL: http://localhost:8080/api");
        logger.info("🏥 Health Check: http://localhost:8080/api/ocr/health");
    }
}
