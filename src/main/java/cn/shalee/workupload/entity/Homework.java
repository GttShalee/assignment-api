package cn.shalee.workupload.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * @author shalee
 */
@Entity
@Table(name = "homework")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Homework {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "class_code", nullable = false)
    private String classCode;
    
    @Column(name = "course_name", length = 256)
    private String courseName;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "attachment_url")
    private String attachmentUrl;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "publish_time", nullable = false)
    private LocalDateTime publishTime;
    
    @Column(nullable = false)
    private LocalDateTime deadline;
    
    @Column(name = "total_score", nullable = false)
    private Integer totalScore;
    
    @Column(nullable = false)
    // 0: 草稿, 1: 已发布, 2: 已截止
    private Integer status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 