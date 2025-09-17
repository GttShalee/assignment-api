package cn.shalee.workupload.controller;

import cn.shalee.workupload.service.HomeworkDeadlineReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员功能控制器
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final HomeworkDeadlineReminderService deadlineReminderService;
    
    /**
     * 手动触发作业截止提醒检查
     */
    @PostMapping("/trigger-deadline-reminders")
    public ResponseEntity<String> triggerDeadlineReminders() {
        log.info("管理员手动触发作业截止提醒检查");
        
        try {
            deadlineReminderService.manualCheckDeadlineReminders();
            return ResponseEntity.ok("作业截止提醒检查已触发");
        } catch (Exception e) {
            log.error("手动触发作业截止提醒失败", e);
            return ResponseEntity.internalServerError().body("触发失败: " + e.getMessage());
        }
    }
    
    /**
     * 为指定作业发送截止提醒
     */
    @PostMapping("/homework/{homeworkId}/deadline-reminder")
    public ResponseEntity<String> sendDeadlineReminderForHomework(@PathVariable Long homeworkId) {
        log.info("管理员为指定作业发送截止提醒: homeworkId={}", homeworkId);
        
        try {
            deadlineReminderService.sendDeadlineReminderForHomework(homeworkId);
            return ResponseEntity.ok("作业截止提醒已发送");
        } catch (Exception e) {
            log.error("发送作业截止提醒失败: homeworkId={}", homeworkId, e);
            return ResponseEntity.internalServerError().body("发送失败: " + e.getMessage());
        }
    }
}
