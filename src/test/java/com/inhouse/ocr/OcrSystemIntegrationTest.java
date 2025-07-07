package com.inhouse.ocr;

import com.inhouse.ocr.entity.OcrJob;
import com.inhouse.ocr.repository.OcrJobRepository;
import com.inhouse.ocr.service.ImagePreprocessingService;
import com.inhouse.ocr.service.PdfProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OcrSystemIntegrationTest {
    
    @Autowired
    private OcrJobRepository jobRepository;
    
    @Autowired
    private PdfProcessingService pdfProcessingService;
    
    @Autowired
    private ImagePreprocessingService imagePreprocessingService;
    
    @Test
    void contextLoads() {
        assertNotNull(jobRepository);
        assertNotNull(pdfProcessingService);
        assertNotNull(imagePreprocessingService);
    }
    
    @Test
    void testDatabaseConnection() {
        // Test basic database operations
        long initialCount = jobRepository.count();
        
        // Create a test job
        OcrJob testJob = new OcrJob("TEST_JOB_123", "test.pdf", "/tmp/test.pdf");
        OcrJob savedJob = jobRepository.save(testJob);
        
        assertNotNull(savedJob.getId());
        assertEquals("TEST_JOB_123", savedJob.getJobId());
        assertEquals(OcrJob.JobStatus.PENDING, savedJob.getStatus());
        
        // Verify count increased
        assertEquals(initialCount + 1, jobRepository.count());
        
        // Clean up
        jobRepository.delete(savedJob);
        assertEquals(initialCount, jobRepository.count());
    }
    
    @Test
    void testImagePreprocessingServiceLoads() {
        // Test that the image preprocessing service is properly initialized
        boolean openCvLoaded = imagePreprocessingService.isOpenCvLoaded();
        // Note: OpenCV may not be available in test environment, so we just verify the service exists
        assertNotNull(imagePreprocessingService);
    }
    
    @Test
    void testJobStatusTransitions() {
        OcrJob job = new OcrJob("TRANSITION_TEST", "test.pdf", "/tmp/test.pdf");
        job = jobRepository.save(job);
        
        // Test status transitions
        assertEquals(OcrJob.JobStatus.PENDING, job.getStatus());
        
        job.startProcessing();
        assertEquals(OcrJob.JobStatus.PROCESSING, job.getStatus());
        assertNotNull(job.getStartedAt());
        
        job.completeProcessing();
        assertEquals(OcrJob.JobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedAt());
        assertNotNull(job.getProcessingTimeMs());
        
        // Clean up
        jobRepository.delete(job);
    }
    
    @Test
    void testJobFailure() {
        OcrJob job = new OcrJob("FAILURE_TEST", "test.pdf", "/tmp/test.pdf");
        job = jobRepository.save(job);
        
        job.startProcessing();
        job.failProcessing("Test error message");
        
        assertEquals(OcrJob.JobStatus.FAILED, job.getStatus());
        assertEquals("Test error message", job.getErrorMessage());
        assertNotNull(job.getCompletedAt());
        
        // Clean up
        jobRepository.delete(job);
    }
}
