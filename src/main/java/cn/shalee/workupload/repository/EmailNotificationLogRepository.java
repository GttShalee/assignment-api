package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.EmailNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件通知记录数据访问层
 * @author 31930
 */
@Repository
public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, Long> {
    
    /**
     * 根据作业ID和邮件类型查找发送记录
     */
    List<EmailNotificationLog> findByHomeworkIdAndEmailType(Long homeworkId, Integer emailType);
    
    /**
     * 根据作业ID、收件人和邮件类型查找发送记录（防重复发送）
     */
    boolean existsByHomeworkIdAndRecipientEmailAndEmailType(Long homeworkId, String recipientEmail, Integer emailType);
    
    /**
     * 根据作业ID查找所有发送记录
     */
    List<EmailNotificationLog> findByHomeworkId(Long homeworkId);
    
    /**
     * 根据发送状态查找记录
     */
    List<EmailNotificationLog> findBySendStatus(Integer sendStatus);
    
    /**
     * 根据时间范围查找记录
     */
    List<EmailNotificationLog> findBySendTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计发送成功的邮件数量
     */
    long countByHomeworkIdAndEmailTypeAndSendStatus(Long homeworkId, Integer emailType, Integer sendStatus);
}
