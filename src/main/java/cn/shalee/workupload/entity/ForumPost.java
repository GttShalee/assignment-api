package cn.shalee.workupload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 * @author 31930
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_post")
public class ForumPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;
    
    @Column(name = "class_code", nullable = false, length = 20)
    private String classCode;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    @Column(name = "attachment_name", length = 255)
    private String attachmentName;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "is_top", nullable = false)
    private Boolean isTop = false;
    
    @Column(name = "is_hot", nullable = false)
    private Boolean isHot = false;
    
    @Column(nullable = false)
    private Integer status = 1; // 0-已删除，1-正常，2-隐藏
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (replyCount == null) replyCount = 0;
        if (viewCount == null) viewCount = 0;
        if (isTop == null) isTop = false;
        if (isHot == null) isHot = false;
        if (status == null) status = 1;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

