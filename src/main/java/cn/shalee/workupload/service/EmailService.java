package cn.shalee.workupload.service;

import cn.shalee.workupload.entity.Homework;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 邮件服务
 * @author shalee
 */
@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String to, String code) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("您的登录验证码");
            // 设置为HTML内容，避免纯文本被过滤
            helper.setText(
                    "<html><body><p>邮箱验证码：<b>" + code + "</b>，5分钟内有效。</p></body></html>",
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 发送作业发布提醒邮件
     */
    public void sendHomeworkPublishedNotification(String to, String studentName, Homework homework) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【作业通知】新作业发布提醒");
            
            String htmlContent = buildHomeworkPublishedEmailContent(studentName, homework);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("作业发布提醒邮件发送成功: to={}, homeworkId={}", to, homework.getId());
        } catch (MessagingException e) {
            log.error("发送作业发布提醒邮件失败: to={}, homeworkId={}, error={}", to, homework.getId(), e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 发送作业截止提醒邮件
     */
    public void sendHomeworkDeadlineNotification(String to, String studentName, Homework homework) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【作业提醒】作业即将截止");
            
            String htmlContent = buildHomeworkDeadlineEmailContent(studentName, homework);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("作业截止提醒邮件发送成功: to={}, homeworkId={}", to, homework.getId());
        } catch (MessagingException e) {
            log.error("发送作业截止提醒邮件失败: to={}, homeworkId={}, error={}", to, homework.getId(), e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }


    /**
     * 构建作业发布邮件内容
     */
    private String buildHomeworkPublishedEmailContent(String studentName, Homework homework) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

        return "<html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; background-color: #000000; font-family: -apple-system, BlinkMacSystemFont, \"SF Pro Display\", \"Helvetica Neue\", Arial, sans-serif;'>" +
                
                // Container
                "<div style='max-width: 600px; margin: 0 auto; background-color: #000000;'>" +
                
                // Header Section
                "<div style='padding: 60px 24px 40px; text-align: center;'>" +
                "<div style='color: #ffffff; font-size: 14px; font-weight: 500; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 16px; opacity: 0.6;'>新作业发布提醒</div>" +
                "<h1 style='margin: 0; color: #ffffff; font-size: 48px; font-weight: 700; letter-spacing: -2px; line-height: 1.1; margin-bottom: 12px;'>" + homework.getTitle() + "</h1>" +
                "<div style='color: #ffffff; font-size: 18px; font-weight: 400; opacity: 0.8;'>Hi " + studentName + "</div>" +
                "</div>" +
                
                // Content Card
                "<div style='background-color: #ffffff; margin: 0 24px; border-radius: 16px; overflow: hidden;'>" +
                
                // Key Info Section
                "<div style='padding: 40px 32px 30px;'>" +
                
                // Due Date - Most Important
                "<div style='text-align: center; padding: 24px 0; border-bottom: 1px solid #f5f5f5; margin-bottom: 32px;'>" +
                "<div style='color: #86868b; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px;'>截止时间</div>" +
                "<div style='color: #1d1d1f; font-size: 36px; font-weight: 700; letter-spacing: -1.5px; line-height: 1.1;'>" + homework.getDeadline().format(formatter) + "</div>" +
                "</div>" +
                
                // Details Grid
                "<div style='display: table; width: 100%; margin-bottom: 32px;'>" +
                "<div style='display: table-row;'>" +
                "<div style='display: table-cell; width: 50%; padding-right: 16px;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px;'>班级</div>" +
                "<div style='color: #1d1d1f; font-size: 20px; font-weight: 600; letter-spacing: -0.3px;'>" + "计科23-2" + "</div>" +
                "</div>" +
                "<div style='display: table-cell; width: 50%;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px;'>Points</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                
                // Requirements
                (homework.getDescription() != null && !homework.getDescription().trim().isEmpty() ? 
                "<div style='margin-bottom: 32px;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px;'>作业内容</div>" +
                "<div style='color: #1d1d1f; font-size: 16px; line-height: 1.5; font-weight: 400;'>" + homework.getDescription() + "</div>" +
                "</div>" : "") +
                
                // System Link
                "<div style='text-align: center; padding: 32px 0; border-top: 1px solid #f5f5f5; border-bottom: 1px solid #f5f5f5;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 16px;'>点击快速提交</div>" +
                "<a href='http://101.201.46.184:3000/' style='display: inline-block; background-color: #1d1d1f; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: 600; padding: 16px 32px; border-radius: 12px; letter-spacing: -0.2px; transition: all 0.2s ease;'>访问提交系统</a>" +
                "</div>" +
                
                // Tips
                "<div style='padding: 32px 0;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px;'>提醒</div>" +
                "<ul style='margin: 0; padding: 0; list-style: none; color: #1d1d1f; font-size: 14px; line-height: 1.6;'>" +
                "<li style='margin-bottom: 8px; padding-left: 16px; position: relative;'>请在截止时间前完成并提交作业</li>" +
                "<li style='margin-bottom: 8px; padding-left: 16px; position: relative;'>注意文件命名规范</li>" +
                "<li style='padding-left: 16px; position: relative;'>如有疑问请及时微信联系学委</li>" +
                "</ul>" +
                "</div>" +
                
                "</div>" +
                "</div>" +
                
                // Footer
                "<div style='padding: 40px 24px 60px; text-align: center;'>" +
                "<div style='color: #ffffff; font-size: 12px; opacity: 0.5; line-height: 1.4;'>此邮件由作业管理系统自动发送，请勿回复</div>" +
                "</div>" +
                
                "</div>" +
                
                // Mobile Styles
                "<style>" +
                "@media only screen and (max-width: 600px) {" +
                "  .container { margin: 0 !important; }" +
                "  .header { padding: 40px 20px 30px !important; }" +
                "  .title { font-size: 36px !important; }" +
                "  .card { margin: 0 16px !important; }" +
                "  .card-content { padding: 30px 24px 20px !important; }" +
                "  .due-date { font-size: 28px !important; }" +
                "  .grid { display: block !important; }" +
                "  .grid-item { display: block !important; width: 100% !important; margin-bottom: 20px; }" +
                "}" +
                "</style>" +
                
                "</body></html>";
    }
    
    /**
     * 构建作业截止提醒邮件内容
     */
    private String buildHomeworkDeadlineEmailContent(String studentName, Homework homework) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        
        return "<html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; background-color: #000000; font-family: -apple-system, BlinkMacSystemFont, \"SF Pro Display\", \"Helvetica Neue\", Arial, sans-serif;'>" +
                
                // Container
                "<div style='max-width: 600px; margin: 0 auto; background-color: #000000;'>" +
                
                // Header Section - Urgent
                "<div style='padding: 60px 24px 40px; text-align: center;'>" +
                "<div style='background-color: #ffffff; color: #000000; display: inline-block; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 24px;'>Urgent</div>" +
                "<h1 style='margin: 0; color: #ffffff; font-size: 48px; font-weight: 700; letter-spacing: -2px; line-height: 1.1; margin-bottom: 12px;'>" + homework.getTitle() + "</h1>" +
                "<div style='color: #ffffff; font-size: 18px; font-weight: 400; opacity: 0.8; margin-bottom: 16px;'>Hi " + studentName + "</div>" +
                "<div style='color: #ffffff; font-size: 16px; font-weight: 500; opacity: 0.9;'>作业将在 2 小时内截止</div>" +
                "</div>" +
                
                // Content Card
                "<div style='background-color: #ffffff; margin: 0 24px; border-radius: 16px; overflow: hidden;'>" +
                
                // Urgent Banner
                "<div style='background-color: #000000; padding: 24px; text-align: center;'>" +
                "<div style='color: #ffffff; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;'>即将截止</div>" +
                "</div>" +
                
                // Key Info Section
                "<div style='padding: 40px 32px 30px;'>" +
                
                // Due Date - Critical
                "<div style='text-align: center; padding: 32px 24px; background-color: #000000; border-radius: 12px; margin-bottom: 32px;'>" +
                "<div style='color: #ffffff; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; opacity: 0.8;'>截止时间</div>" +
                "<div style='color: #ffffff; font-size: 42px; font-weight: 700; letter-spacing: -2px; line-height: 1.1;'>" + homework.getDeadline().format(formatter) + "</div>" +
                "</div>" +
                
                // Details Grid
                "<div style='display: table; width: 100%; margin-bottom: 32px;'>" +
                "<div style='display: table-row;'>" +
                "<div style='display: table-cell; width: 50%; padding-right: 16px;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px;'>班级</div>" +
                "<div style='color: #1d1d1f; font-size: 20px; font-weight: 600; letter-spacing: -0.3px;'>" + "计科23-2" + "</div>" +
                "</div>" +
                "<div style='display: table-cell; width: 50%;'>" +
                "</div>" +
                "</div>" +
                "</div>" +
                
                // Requirements
                (homework.getDescription() != null && !homework.getDescription().trim().isEmpty() ? 
                "<div style='margin-bottom: 32px;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px;'>作业要求</div>" +
                "<div style='color: #1d1d1f; font-size: 16px; line-height: 1.5; font-weight: 400;'>" + homework.getDescription() + "</div>" +
                "</div>" : "") +
                
                // System Link - Urgent Style
                "<div style='text-align: center; padding: 32px 0; background-color: #000000; border-radius: 12px; margin-bottom: 32px;'>" +
                "<div style='color: #ffffff; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 16px; opacity: 0.8;'>Submit Now</div>" +
                "<a href='http://101.201.46.184:3000/' style='display: inline-block; background-color: #ffffff; color: #000000; text-decoration: none; font-size: 16px; font-weight: 600; padding: 16px 32px; border-radius: 12px; letter-spacing: -0.2px; transition: all 0.2s ease;'>立即提交作业</a>" +
                "</div>" +
                
                // Action Items
                "<div style='padding: 0 0 32px 0;'>" +
                "<div style='color: #86868b; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px;'>你需要做的</div>" +
                "<ul style='margin: 0; padding: 0; list-style: none; color: #1d1d1f; font-size: 14px; line-height: 1.6;'>" +
                "<li style='margin-bottom: 8px; padding-left: 16px; position: relative; font-weight: 600;'>立即登录系统提交作业</li>" +
                "<li style='margin-bottom: 8px; padding-left: 16px; position: relative;'>逾期提交可能影响成绩</li>" +
                "<li style='padding-left: 16px; position: relative;'>如遇技术问题请及时微信联系我</li>" +
                "</ul>" +
                "</div>" +
                
                "</div>" +
                "</div>" +
                
                // Footer
                "<div style='padding: 40px 24px 60px; text-align: center;'>" +
                "<div style='color: #ffffff; font-size: 12px; opacity: 0.5; line-height: 1.4;'>此邮件由作业管理系统自动发送，请勿直接回复</div>" +
                "</div>" +
                
                "</div>" +
                
                // Mobile Styles
                "<style>" +
                "@media only screen and (max-width: 600px) {" +
                "  .container { margin: 0 !important; }" +
                "  .header { padding: 40px 20px 30px !important; }" +
                "  .title { font-size: 36px !important; }" +
                "  .card { margin: 0 16px !important; }" +
                "  .card-content { padding: 30px 24px 20px !important; }" +
                "  .due-date { font-size: 32px !important; }" +
                "  .grid { display: block !important; }" +
                "  .grid-item { display: block !important; width: 100% !important; margin-bottom: 20px; }" +
                "}" +
                "</style>" +
                
                "</body></html>";
    }
}
