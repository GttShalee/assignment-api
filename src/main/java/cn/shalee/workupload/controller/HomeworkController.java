package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.CreateHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkResponse;
import cn.shalee.workupload.service.HomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {
    
    private final HomeworkService homeworkService;
    
    @PostMapping
    public ResponseEntity<HomeworkResponse> createHomework(@Valid @RequestBody CreateHomeworkRequest request) {
        log.info("收到创建作业请求: title={}", request.getTitle());
        HomeworkResponse response = homeworkService.createHomework(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<HomeworkResponse>> getHomeworkList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业列表请求: userEmail={}, page={}, pageSize={}", 
                userEmail, page, pageSize);
        
        Page<HomeworkResponse> response = homeworkService.getHomeworkListByUser(userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HomeworkResponse> getHomeworkDetail(@PathVariable Long id) {
        log.info("收到获取作业详情请求: id={}", id);
        HomeworkResponse response = homeworkService.getHomeworkDetail(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HomeworkResponse> updateHomework(
            @PathVariable Long id, 
            @Valid @RequestBody CreateHomeworkRequest request) {
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到更新作业请求: id={}, userEmail={}", id, userEmail);
        HomeworkResponse response = homeworkService.updateHomework(id, request, userEmail);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long id) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到删除作业请求: id={}, userEmail={}", id, userEmail);
        homeworkService.deleteHomework(id, userEmail);
        return ResponseEntity.ok().build();
    }
} 