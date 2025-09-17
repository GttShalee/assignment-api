package cn.shalee.workupload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 邮件通知发送记录实体
 * @author 31930
 */
@Entity
@Table(name = "email_notification_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 作业ID
     */
    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;
    
    /**
     * 收件人邮箱
     */
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    /**
     * 收件人学号
     */
    @Column(name = "recipient_student_id", nullable = false)
    private String recipientStudentId;
    
    /**
     * 邮件类型 (1-发布提醒, 2-截止提醒)
     */
    @Column(name = "email_type", nullable = false)
    private Integer emailType;
    
    /**
     * 邮件主题
     */
    @Column(name = "email_subject", nullable = false)
    private String emailSubject;
    
    /**
     * 邮件内容
     */
    @Column(name = "email_content", columnDefinition = "TEXT")
    private String emailContent;
    
    /**
     * 发送状态 (1-成功, 0-失败)
     */
    @Column(name = "send_status", nullable = false)
    private Integer sendStatus;
    
    /**
     * 失败原因
     */
    @Column(name = "error_message")
    private String errorMessage;
    
    /**
     * 发送时间
     */
    @CreationTimestamp
    @Column(name = "send_time")
    private LocalDateTime sendTime;
}
