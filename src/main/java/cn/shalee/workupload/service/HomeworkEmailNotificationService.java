package cn.shalee.workupload.service;

import cn.shalee.workupload.entity.EmailNotificationLog;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.EmailNotificationLogRepository;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.util.CourseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 作业邮件通知服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkEmailNotificationService {
    
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EmailNotificationLogRepository emailNotificationLogRepository;
    
    /**
     * 异步发送作业发布通知（给选了该课程的班级学生）
     */
    @Async
    public void sendHomeworkPublishedNotifications(Homework homework) {
        log.info("开始发送作业发布通知: homeworkId={}, classCode={}, courseCode={}", 
                homework.getId(), homework.getClassCode(), homework.getCourseCode());
        
        // 获取班级所有学生 (role_type = 1)
        List<User> allStudents = userRepository.findByClassCodeAndRoleType(homework.getClassCode(), 1);
        log.info("班级学生总数: {}", allStudents.size());
        
        // 过滤出选了该课程的学生
        List<User> targetStudents = allStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        log.debug("学生未选课，跳过邮件发送: studentId={}", student.getStudentId());
                        return false;
                    }
                    
                    Integer homeworkCourseCode = homework.getCourseCode();
                    if (homeworkCourseCode == null || homeworkCourseCode == 0) {
                        log.debug("作业未指定课程代码，发送给所有学生: homeworkId={}", homework.getId());
                        return true;
                    }
                    
                    boolean isSelected = CourseUtils.isCourseSelected(studentCourses, homeworkCourseCode);
                    if (!isSelected) {
                        log.debug("学生未选择该课程，跳过邮件发送: studentId={}, studentCourses={}, homeworkCourseCode={}", 
                                student.getStudentId(), studentCourses, homeworkCourseCode);
                    }
                    return isSelected;
                })
                .toList();
        
        log.info("选了该课程的学生数量: {} / {}", targetStudents.size(), allStudents.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (User student : targetStudents) {
            try {
                // 检查是否已经发送过（防重复）
                if (emailNotificationLogRepository.existsByHomeworkIdAndRecipientEmailAndEmailType(
                        homework.getId(), student.getEmail(), 1)) {
                    log.info("作业发布通知已发送过，跳过: studentId={}, homeworkId={}", 
                            student.getStudentId(), homework.getId());
                    continue;
                }
                
                // 发送邮件
                emailService.sendHomeworkPublishedNotification(student.getEmail(), student.getRealName(), homework);
                
                // 记录发送成功
                saveEmailLog(homework.getId(), student, 1, "【作业通知】新作业发布提醒", 
                           "作业发布通知", 1, null);
                
                successCount++;
                log.info("作业发布通知发送成功: studentId={}, email={}", student.getStudentId(), student.getEmail());
                
                // 为了避免邮件服务器压力，每发送一封邮件后稍微休息
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("作业发布通知发送失败: studentId={}, email={}, error={}", 
                        student.getStudentId(), student.getEmail(), e.getMessage());
                
                // 记录发送失败
                saveEmailLog(homework.getId(), student, 1, "【作业通知】新作业发布提醒", 
                           "作业发布通知", 0, e.getMessage());
                failCount++;
            }
        }
        
        log.info("作业发布通知发送完成: homeworkId={}, 目标学生={}, 成功={}, 失败={}", 
                homework.getId(), targetStudents.size(), successCount, failCount);
    }
    
    /**
     * 异步发送作业截止提醒（给未提交且选了该课程的学生）
     */
    @Async
    public void sendHomeworkDeadlineNotifications(Homework homework, List<User> unsubmittedStudents) {
        log.info("开始发送作业截止提醒: homeworkId={}, courseCode={}, 未提交学生数量={}", 
                homework.getId(), homework.getCourseCode(), unsubmittedStudents.size());
        
        // 过滤出选了该课程的未提交学生
        List<User> targetStudents = unsubmittedStudents.stream()
                .filter(student -> {
                    Integer studentCourses = student.getCourses();
                    if (studentCourses == null || studentCourses == 0) {
                        log.debug("学生未选课，跳过截止提醒: studentId={}", student.getStudentId());
                        return false;
                    }
                    
                    Integer homeworkCourseCode = homework.getCourseCode();
                    if (homeworkCourseCode == null || homeworkCourseCode == 0) {
                        log.debug("作业未指定课程代码，发送给所有未提交学生: homeworkId={}", homework.getId());
                        return true;
                    }
                    
                    boolean isSelected = CourseUtils.isCourseSelected(studentCourses, homeworkCourseCode);
                    if (!isSelected) {
                        log.debug("学生未选择该课程，跳过截止提醒: studentId={}, studentCourses={}, homeworkCourseCode={}", 
                                student.getStudentId(), studentCourses, homeworkCourseCode);
                    }
                    return isSelected;
                })
                .toList();
        
        log.info("选了该课程的未提交学生数量: {} / {}", targetStudents.size(), unsubmittedStudents.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (User student : targetStudents) {
            try {
                // 检查是否已经发送过截止提醒（防重复）
                if (emailNotificationLogRepository.existsByHomeworkIdAndRecipientEmailAndEmailType(
                        homework.getId(), student.getEmail(), 2)) {
                    log.info("作业截止提醒已发送过，跳过: studentId={}, homeworkId={}", 
                            student.getStudentId(), homework.getId());
                    continue;
                }
                
                // 发送邮件
                emailService.sendHomeworkDeadlineNotification(student.getEmail(), student.getRealName(), homework);
                
                // 记录发送成功
                saveEmailLog(homework.getId(), student, 2, "【作业提醒】作业即将截止", 
                           "作业截止提醒", 1, null);
                
                successCount++;
                log.info("作业截止提醒发送成功: studentId={}, email={}", student.getStudentId(), student.getEmail());
                
                // 为了避免邮件服务器压力，每发送一封邮件后稍微休息
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("作业截止提醒发送失败: studentId={}, email={}, error={}", 
                        student.getStudentId(), student.getEmail(), e.getMessage());
                
                // 记录发送失败
                saveEmailLog(homework.getId(), student, 2, "【作业提醒】作业即将截止", 
                           "作业截止提醒", 0, e.getMessage());
                failCount++;
            }
        }
        
        log.info("作业截止提醒发送完成: homeworkId={}, 目标学生={}, 成功={}, 失败={}", 
                homework.getId(), targetStudents.size(), successCount, failCount);
    }
    
    /**
     * 保存邮件发送记录
     */
    private void saveEmailLog(Long homeworkId, User recipient, Integer emailType, String subject, 
                             String content, Integer sendStatus, String errorMessage) {
        try {
            EmailNotificationLog log = EmailNotificationLog.builder()
                    .homeworkId(homeworkId)
                    .recipientEmail(recipient.getEmail())
                    .recipientStudentId(recipient.getStudentId())
                    .emailType(emailType)
                    .emailSubject(subject)
                    .emailContent(content)
                    .sendStatus(sendStatus)
                    .errorMessage(errorMessage)
                    .build();
            
            emailNotificationLogRepository.save(log);
        } catch (Exception e) {
            log.error("保存邮件发送记录失败: homeworkId={}, recipientEmail={}, error={}", 
                    homeworkId, recipient.getEmail(), e.getMessage());
        }
    }
}
