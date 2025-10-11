package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.GradeHomeworkRequest;
import cn.shalee.workupload.dto.request.SubmitHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkSubmissionResponse;
import cn.shalee.workupload.dto.response.UnsubmittedMemberResponse;
import cn.shalee.workupload.dto.response.UserSubmissionHistoryResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.service.HomeworkSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 作业提交控制器
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/homework-submission")
@RequiredArgsConstructor
public class HomeworkSubmissionController {
    
    private final HomeworkSubmissionService homeworkSubmissionService;
    private final UserRepository userRepository;
    
    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public ResponseEntity<HomeworkSubmissionResponse> submitHomework(
            @RequestParam("homework_id") Long homeworkId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "remarks", required = false) String remarks) {
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到提交作业请求: homeworkId={}, userEmail={}, hasFile={}", homeworkId, userEmail, file != null);
        
        // 构建请求对象
        SubmitHomeworkRequest request = new SubmitHomeworkRequest();
        request.setHomeworkId(homeworkId);
        request.setRemarks(remarks);
        
        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            try {
                // 获取作业信息以确定文件夹路径
                Homework homework = homeworkSubmissionService.getHomeworkById(homeworkId);
                if (homework == null) {
                    return ResponseEntity.badRequest().build();
                }
                
                // 构建作业文件夹路径：uploads/homework/班级代码-作业名称-日期/
                String folderName = generateHomeworkFolderName(homework);
                
                Path uploadPath = Paths.get("uploads/homework/" + folderName);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    log.info("创建作业文件夹: {}", uploadPath);
                }
                
                // 生成文件名：
                User user = homeworkSubmissionService.getUserByEmail(userEmail);
                String originalFilename = file.getOriginalFilename();
                String filename = originalFilename;

                // 如果文件已存在，添加时间戳
                Path filePath = uploadPath.resolve(filename);
                if (Files.exists(filePath)) {
                    String timestamp2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
                    filename = user.getStudentId() + "_" + timestamp2 + "_" + originalFilename;
                    filePath = uploadPath.resolve(filename);
                }
                
                // 保存文件
                Files.copy(file.getInputStream(), filePath);
                
                // 检查文件名是否被修改（添加了时间戳前缀）
                boolean filenameModified = !filename.equals(originalFilename);
                if (filenameModified) {
                    // 文件名被修改，增加fuck计数
                    Integer currentFuck = user.getFuck() != null ? user.getFuck() : 0;
                    user.setFuck(currentFuck + 1);
                    userRepository.save(user);
                    log.info("用户文件名不规范，增加fuck计数: userId={}, studentId={}, fuck={}", 
                            user.getId(), user.getStudentId(), user.getFuck());
                }
                
                String fileUrl = "/uploads/homework/" + folderName + "/" + filename;
                request.setSubmissionFileUrl(fileUrl);
                request.setSubmissionFileName(originalFilename);
                
                log.info("作业文件上传成功: url={}, originalName={}, folder={}, filenameModified={}", 
                        fileUrl, originalFilename, folderName, filenameModified);
                
            } catch (IOException e) {
                log.error("作业文件上传失败", e);
                return ResponseEntity.internalServerError().build();
            }
        }
        
        HomeworkSubmissionResponse response = homeworkSubmissionService.submitHomework(request, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我的作业提交列表
     */
    @GetMapping("/my")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getMySubmissions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取我的作业提交列表请求: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        Page<HomeworkSubmissionResponse> response = homeworkSubmissionService.getMySubmissions(userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户历史提交记录
     */
    @GetMapping("/my/history")
    public ResponseEntity<Page<UserSubmissionHistoryResponse>> getUserSubmissionHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取用户历史提交记录请求: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        Page<UserSubmissionHistoryResponse> response = homeworkSubmissionService.getUserSubmissionHistory(userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取作业提交详情
     */
    @GetMapping("/{submissionId}")
    public ResponseEntity<HomeworkSubmissionResponse> getSubmissionDetail(@PathVariable Long submissionId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业提交详情请求: submissionId={}, userEmail={}", submissionId, userEmail);
        
        HomeworkSubmissionResponse response = homeworkSubmissionService.getSubmissionDetail(submissionId, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取作业的所有提交记录（学委/管理员）
     */
    @GetMapping("/homework/{homeworkId}")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getHomeworkSubmissions(
            @PathVariable Long homeworkId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业提交记录请求: homeworkId={}, userEmail={}, page={}, pageSize={}", 
                homeworkId, userEmail, page, pageSize);
        
        Page<HomeworkSubmissionResponse> response = homeworkSubmissionService.getHomeworkSubmissions(homeworkId, userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批改作业（学委/管理员）
     */
    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<HomeworkSubmissionResponse> gradeHomework(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeHomeworkRequest request) {
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到批改作业请求: submissionId={}, userEmail={}, score={}", submissionId, userEmail, request.getScore());
        
        HomeworkSubmissionResponse response = homeworkSubmissionService.gradeHomework(submissionId, request, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取作业统计信息（学委/管理员）
     */
    @GetMapping("/homework/{homeworkId}/stats")
    public ResponseEntity<Object> getHomeworkStats(@PathVariable Long homeworkId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业统计信息请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        Object response = homeworkSubmissionService.getHomeworkStats(homeworkId, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取作业提交列表（学委/管理员）
     */
    @GetMapping("/list/{homeworkId}")
    public ResponseEntity<Page<HomeworkSubmissionResponse>> getHomeworkSubmissionList(
            @PathVariable Long homeworkId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize) {
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业提交列表请求: homeworkId={}, userEmail={}, page={}, pageSize={}", 
                homeworkId, userEmail, page, pageSize);
        
        Page<HomeworkSubmissionResponse> response = homeworkSubmissionService.getHomeworkSubmissions(homeworkId, userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 下载作业提交包（学委/管理员）
     */
    @GetMapping("/{homeworkId}/download")
    public ResponseEntity<byte[]> downloadHomeworkSubmissions(@PathVariable Long homeworkId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到下载作业提交包请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        try {
            // 获取ZIP文件内容
            byte[] zipContent = homeworkSubmissionService.downloadHomeworkSubmissions(homeworkId, userEmail);
            
            // 获取文件名
            String fileName = homeworkSubmissionService.getHomeworkSubmissionsZipFileName(homeworkId, userEmail);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(zipContent.length);
            
            log.info("作业提交包下载成功: homeworkId={}, fileName={}, size={} bytes", 
                    homeworkId, fileName, zipContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipContent);
                    
        } catch (Exception e) {
            log.error("下载作业提交包失败: homeworkId={}, error={}", homeworkId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取作业未交成员列表（学委/管理员）
     */
    @GetMapping("/homework/{homeworkId}/unsubmitted-members")
    public ResponseEntity<List<UnsubmittedMemberResponse>> getUnsubmittedMembers(@PathVariable Long homeworkId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取作业未交成员列表请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        try {
            List<UnsubmittedMemberResponse> response = homeworkSubmissionService.getUnsubmittedMembers(homeworkId, userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取作业未交成员列表失败: homeworkId={}, error={}", homeworkId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 撤回作业提交（学生）
     */
    @DeleteMapping("/homework/{homeworkId}/withdraw")
    public ResponseEntity<String> withdrawHomeworkSubmission(@PathVariable Long homeworkId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到撤回作业提交请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        try {
            homeworkSubmissionService.withdrawHomeworkSubmission(homeworkId, userEmail);
            log.info("撤回作业提交成功: homeworkId={}, userEmail={}", homeworkId, userEmail);
            return ResponseEntity.ok("撤回成功");
        } catch (Exception e) {
            log.error("撤回作业提交失败: homeworkId={}, error={}", homeworkId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("撤回失败: " + e.getMessage());
        }
    }
    
    /**
     * Fuck You 接口 - 增加用户fuck计数
     */
    @PostMapping("/fuck_you")
    public ResponseEntity<Object> fuckYou() {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到Fuck You请求: userEmail={}", userEmail);
        
        try {
            // 从数据库获取当前用户
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 增加fuck计数
            Integer currentFuck = user.getFuck() != null ? user.getFuck() : 0;
            user.setFuck(currentFuck + 1);
            userRepository.save(user);
            
            log.info("Fuck You计数增加成功: userId={}, studentId={}, newFuckCount={}", 
                    user.getId(), user.getStudentId(), user.getFuck());
            
            // 返回成功响应
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Fuck计数增加成功",
                "data", java.util.Map.of(
                    "studentId", user.getStudentId(),
                    "realName", user.getRealName(),
                    "fuckCount", user.getFuck()
                )
            ));
            
        } catch (Exception e) {
            log.error("Fuck You计数增加失败: userEmail={}, error={}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(500).body(java.util.Map.of(
                "message", "操作失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 下载当前用户的作业提交文件 - 通过作业ID直接获取
     */
    @GetMapping("/download/{homeworkId}")
    public ResponseEntity<Void> downloadMyHomeworkFile(@PathVariable Long homeworkId, HttpServletRequest request) {
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("收到下载我的作业文件请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
            
            // 获取当前用户在该作业下的提交记录
            HomeworkSubmissionResponse submission = homeworkSubmissionService.getMySubmissionByHomeworkId(homeworkId, userEmail);
            
            if (submission == null || submission.getSubmissionFileUrl() == null || submission.getSubmissionFileUrl().isEmpty()) {
                log.warn("用户没有提交文件: homeworkId={}, userEmail={}", homeworkId, userEmail);
                return ResponseEntity.notFound().build();
            }
            
            // 构建静态资源URL
            String fileUrl = submission.getSubmissionFileUrl();
            if (!fileUrl.startsWith("/")) {
                fileUrl = "/" + fileUrl; // 确保以斜杠开头
            }
            
            // 构建完整的下载URL
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String downloadUrl = baseUrl + fileUrl;
            
            log.info("重定向到静态资源下载: homeworkId={}, userEmail={}, url={}", homeworkId, userEmail, downloadUrl);
            
            // 返回重定向响应
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(downloadUrl))
                    .build();
                    
        } catch (Exception e) {
            log.error("下载我的作业文件失败: homeworkId={}, error={}", homeworkId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成作业文件夹名称
     * 确保与HomeworkService中的逻辑一致
     */
    private String generateHomeworkFolderName(Homework homework) {
        String timestamp = homework.getPublishTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String folderName = homework.getClassCode() + "-" + homework.getTitle() + "-" + timestamp;
        // 保留中文字符，只替换可能导致文件系统问题的特殊字符
        // 替换文件系统不支持的字符：\ / : * ? " < > |
        folderName = folderName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return folderName;
    }
} 