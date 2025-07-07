package com.inhouse.ocr.repository;

import com.inhouse.ocr.entity.OcrJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OcrJobRepository extends JpaRepository<OcrJob, Long> {
    
    Optional<OcrJob> findByJobId(String jobId);
    
    List<OcrJob> findByStatus(OcrJob.JobStatus status);
    
    List<OcrJob> findByStatusOrderByCreatedAtDesc(OcrJob.JobStatus status);
    
    @Query("SELECT j FROM OcrJob j WHERE j.createdAt >= :since ORDER BY j.createdAt DESC")
    List<OcrJob> findRecentJobs(@Param("since") LocalDateTime since);
    
    @Query("SELECT j FROM OcrJob j WHERE j.status = :status AND j.createdAt <= :before")
    List<OcrJob> findOldJobsByStatus(@Param("status") OcrJob.JobStatus status, 
                                     @Param("before") LocalDateTime before);
    
    @Query("SELECT COUNT(j) FROM OcrJob j WHERE j.status = :status")
    long countByStatus(@Param("status") OcrJob.JobStatus status);
    
    @Query("SELECT AVG(j.processingTimeMs) FROM OcrJob j WHERE j.status = 'COMPLETED' AND j.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime();
    
    @Query("SELECT AVG(j.overallConfidence) FROM OcrJob j WHERE j.status = 'COMPLETED' AND j.overallConfidence IS NOT NULL")
    Double getAverageConfidence();
    
    void deleteByJobId(String jobId);
}
