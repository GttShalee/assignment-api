package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.CreateHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkResponse;
import cn.shalee.workupload.service.HomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("收到获取作业列表请求: classCode={}, status={}, page={}, pageSize={}", 
                classCode, status, page, pageSize);
        
        Page<HomeworkResponse> response = homeworkService.getHomeworkList(classCode, status, page, pageSize);
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
        
        log.info("收到更新作业请求: id={}", id);
        HomeworkResponse response = homeworkService.updateHomework(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long id) {
        log.info("收到删除作业请求: id={}", id);
        homeworkService.deleteHomework(id);
        return ResponseEntity.ok().build();
    }
} 