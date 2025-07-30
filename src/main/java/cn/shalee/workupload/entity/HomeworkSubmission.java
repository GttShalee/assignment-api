package cn.shalee.workupload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交实体类
 * @author 31930
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "homework_submission")
public class HomeworkSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;
    
    @Column(name = "class_code", nullable = false, length = 20)
    private String classCode;
    
    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;
    
    @Column(name = "submission_time", nullable = false)
    private LocalDateTime submissionTime;
    
    @Column(name = "submission_file_url", length = 500)
    private String submissionFileUrl;
    
    @Column(name = "submission_file_name", length = 255)
    private String submissionFileName;
    
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "submission_status", nullable = false)
    private Integer submissionStatus; // 0-按时提交，1-补交
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 