package cn.shalee.workupload.service;

import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.HomeworkSubmission;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.HomeworkRepository;
import cn.shalee.workupload.repository.HomeworkSubmissionRepository;
import cn.shalee.workupload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 作业截止提醒定时任务服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkDeadlineReminderService {
    
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final UserRepository userRepository;
    private final HomeworkEmailNotificationService emailNotificationService;
    
    /**
     * 定时检查即将截止的作业并发送提醒
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时的0分0秒执行
    public void checkDeadlineAndSendReminders() {
        log.info("开始执行作业截止提醒定时任务");
        
        try {
            // 获取2小时内即将截止的作业
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoHoursLater = now.plusHours(2);
            
            List<Homework> upcomingDeadlineHomeworks = homeworkRepository.findByDeadlineBetween(now, twoHoursLater);
            log.info("找到即将截止的作业数量: {}", upcomingDeadlineHomeworks.size());
            
            for (Homework homework : upcomingDeadlineHomeworks) {
                try {
                    processHomeworkDeadlineReminder(homework);
                } catch (Exception e) {
                    log.error("处理作业截止提醒失败: homeworkId={}, error={}", homework.getId(), e.getMessage(), e);
                }
            }
            
            log.info("作业截止提醒定时任务执行完成");
            
        } catch (Exception e) {
            log.error("作业截止提醒定时任务执行失败", e);
        }
    }
    
    /**
     * 处理单个作业的截止提醒
     */
    private void processHomeworkDeadlineReminder(Homework homework) {
        log.info("处理作业截止提醒: homeworkId={}, title={}, deadline={}", 
                homework.getId(), homework.getTitle(), homework.getDeadline());
        
        // 获取班级所有学生
        List<User> allStudents = userRepository.findByClassCodeAndRoleType(homework.getClassCode(), 1);
        
        // 获取已提交的学生学号
        List<HomeworkSubmission> submissions = homeworkSubmissionRepository.findByHomeworkId(homework.getId());
        Set<String> submittedStudentIds = submissions.stream()
                .map(HomeworkSubmission::getStudentId)
                .collect(Collectors.toSet());
        
        // 筛选出未提交的学生
        List<User> unsubmittedStudents = allStudents.stream()
                .filter(student -> !submittedStudentIds.contains(student.getStudentId()))
                .collect(Collectors.toList());
        
        log.info("作业统计: homeworkId={}, 总学生数={}, 已提交={}, 未提交={}", 
                homework.getId(), allStudents.size(), submittedStudentIds.size(), unsubmittedStudents.size());
        
        if (!unsubmittedStudents.isEmpty()) {
            // 异步发送截止提醒邮件
            emailNotificationService.sendHomeworkDeadlineNotifications(homework, unsubmittedStudents);
        } else {
            log.info("所有学生都已提交作业，无需发送提醒: homeworkId={}", homework.getId());
        }
    }
    
    /**
     * 手动触发截止提醒（用于测试或管理员手动执行）
     */
    public void manualCheckDeadlineReminders() {
        log.info("手动触发作业截止提醒检查");
        checkDeadlineAndSendReminders();
    }
    
    /**
     * 为指定作业发送截止提醒
     */
    public void sendDeadlineReminderForHomework(Long homeworkId) {
        log.info("为指定作业发送截止提醒: homeworkId={}", homeworkId);
        
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new RuntimeException("作业不存在: " + homeworkId));
        
        processHomeworkDeadlineReminder(homework);
    }
}
