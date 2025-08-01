package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.response.ClassMemberResponse;
import cn.shalee.workupload.dto.response.ClassResponse;
import cn.shalee.workupload.service.ClassService;
import cn.shalee.workupload.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级控制器
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {
    
    private final ClassService classService;
    private final TokenValidationService tokenValidationService;
    
    /**
     * 获取班级列表
     */
    @GetMapping
    public ResponseEntity<List<ClassResponse>> getClassList() {
        log.info("收到获取班级列表请求");
        
        List<ClassResponse> response = classService.getClassList();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取班级信息
     */
    @GetMapping("/{classCode}")
    public ResponseEntity<ClassResponse> getClassInfo(@PathVariable String classCode) {
        log.info("收到获取班级信息请求: classCode={}", classCode);
        
        ClassResponse response = classService.getClassInfo(classCode);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取班级成员列表
     */
    @GetMapping("/{classCode}/members")
    public ResponseEntity<Page<ClassMemberResponse>> getClassMembers(
            @PathVariable String classCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize) {
        
        log.info("收到获取班级成员列表请求: classCode={}, page={}, pageSize={}", classCode, page, pageSize);
        
        Page<ClassMemberResponse> response = classService.getClassMembers(classCode, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 强制用户下线（管理员功能）
     */
    @PostMapping("/{classCode}/members/{studentId}/force-logout")
    public ResponseEntity<String> forceUserLogout(
            @PathVariable String classCode,
            @PathVariable String studentId) {
        
        log.info("收到强制用户下线请求: classCode={}, studentId={}", classCode, studentId);
        
        try {
            // 调用服务层执行强制下线
            classService.forceUserLogout(classCode, studentId);
            
            log.info("强制用户下线成功: classCode={}, studentId={}", classCode, studentId);
            return ResponseEntity.ok("强制下线成功");
            
        } catch (Exception e) {
            log.error("强制用户下线失败: classCode={}, studentId={}, error={}", 
                    classCode, studentId, e.getMessage());
            return ResponseEntity.internalServerError().body("强制下线失败: " + e.getMessage());
        }
    }
} 