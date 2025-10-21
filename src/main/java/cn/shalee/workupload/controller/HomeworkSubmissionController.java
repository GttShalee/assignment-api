package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.GradeHomeworkRequest;
import cn.shalee.workupload.dto.request.SubmitHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkSubmissionResponse;
import cn.shalee.workupload.dto.response.SubmissionRecordResponse;
import cn.shalee.workupload.dto.response.UnsubmittedMemberResponse;
import cn.shalee.workupload.dto.response.UserSubmissionHistoryResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.service.HomeworkSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.data.domain.Page;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * 下载指定提交记录的文件 - 学委/管理员（流式传输优化）
     */
    @GetMapping("/submission/{submissionId}/download")
    public ResponseEntity<Resource> downloadSubmissionFile(@PathVariable Long submissionId) {
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("收到下载提交文件请求: submissionId={}, userEmail={}", submissionId, userEmail);
            
            // 获取提交记录详情（包含权限验证）
            HomeworkSubmissionResponse submission = homeworkSubmissionService.getSubmissionDetail(submissionId, userEmail);
            
            if (submission == null || submission.getSubmissionFileUrl() == null || submission.getSubmissionFileUrl().isEmpty()) {
                log.warn("提交记录没有文件: submissionId={}", submissionId);
                return ResponseEntity.notFound().build();
            }
            
            // 使用工具方法解析文件路径（基于jar所在目录）
            String fileUrl = submission.getSubmissionFileUrl();
            Path filePath = resolveFilePath(fileUrl);
            
            log.info("解析文件路径: fileUrl={}, 完整路径={}", fileUrl, filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                log.error("文件不存在: path={}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 直接从文件路径中提取文件名（肯定包含完整的 .docx 等扩展名）
            String fileName = filePath.getFileName().toString();
            
            // 使用 FileSystemResource 进行流式传输（不占用内存）
            Resource resource = new FileSystemResource(filePath);
            long fileSize = Files.size(filePath);
            
            // 设置响应头 - 使用最兼容的方式
            HttpHeaders headers = new HttpHeaders();
            // 强制使用二进制流，不让浏览器猜测文件类型
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 设置文件名，确保扩展名正确
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.builder("attachment")
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );
            // 设置文件大小（让浏览器可以显示下载进度）
            headers.setContentLength(fileSize);
            // 禁用缓存和内容嗅探
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.set("X-Content-Type-Options", "nosniff");
            // 支持断点续传
            headers.set("Accept-Ranges", "bytes");
            
            log.info("提交文件下载开始（流式传输）: submissionId={}, fileName={}, size={} bytes", 
                    submissionId, fileName, fileSize);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载提交文件失败: submissionId={}, error={}", submissionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 下载作业的所有提交文件（打包为ZIP）- 学委/管理员
     */
    @GetMapping("/homework/{homeworkId}/download-all")
    public ResponseEntity<byte[]> downloadAllHomeworkSubmissions(@PathVariable Long homeworkId) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到下载作业提交包请求: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        try {
            // 获取ZIP文件内容
            byte[] zipContent = homeworkSubmissionService.downloadHomeworkSubmissions(homeworkId, userEmail);
            
            // 获取文件名
            String fileName = homeworkSubmissionService.getHomeworkSubmissionsZipFileName(homeworkId, userEmail);
            
            // 设置响应头 - 使用最兼容的方式
            HttpHeaders headers = new HttpHeaders();
            // ZIP 文件也使用二进制流
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 设置文件名，确保 .zip 扩展名正确
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.builder("attachment")
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );
            headers.setContentLength(zipContent.length);
            // 禁用缓存和内容嗅探
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.set("X-Content-Type-Options", "nosniff");
            
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
     * 下载当前用户自己的作业提交文件（流式传输优化）
     */
    @GetMapping("/my/{homeworkId}/download")
    public ResponseEntity<Resource> downloadMyHomeworkFile(@PathVariable Long homeworkId) {
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
            
            // 使用工具方法解析文件路径（基于jar所在目录）
            String fileUrl = submission.getSubmissionFileUrl();
            Path filePath = resolveFilePath(fileUrl);
            
            log.info("解析文件路径: fileUrl={}, 完整路径={}", fileUrl, filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                log.error("文件不存在: path={}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            // 直接从文件路径中提取文件名（肯定包含完整的 .docx 等扩展名）
            String fileName = filePath.getFileName().toString();
            
            // 使用 FileSystemResource 进行流式传输（不占用内存）
            Resource resource = new FileSystemResource(filePath);
            long fileSize = Files.size(filePath);
            
            // 设置响应头 - 使用最兼容的方式
            HttpHeaders headers = new HttpHeaders();
            // 强制使用二进制流，不让浏览器猜测文件类型
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 设置文件名，确保扩展名正确
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.builder("attachment")
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );
            // 设置文件大小（让浏览器可以显示下载进度）
            headers.setContentLength(fileSize);
            // 禁用缓存和内容嗅探
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.set("X-Content-Type-Options", "nosniff");
            // 支持断点续传
            headers.set("Accept-Ranges", "bytes");
            
            log.info("作业文件下载开始（流式传输）: homeworkId={}, userEmail={}, fileName={}, size={} bytes", 
                    homeworkId, userEmail, fileName, fileSize);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载我的作业文件失败: homeworkId={}, error={}", homeworkId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取班级提交记录（班级空间展示）
     * 返回用户名、提交的作业名称、提交时间、提交状态标记、是否首位提交标记
     */
    @GetMapping("/records")
    public ResponseEntity<Page<SubmissionRecordResponse>> getAllSubmissionRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        // 移除最大限制，允许无限制显示条数
        
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到获取班级提交记录请求: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        Page<SubmissionRecordResponse> response = homeworkSubmissionService.getAllSubmissionRecords(userEmail, page, pageSize);
        return ResponseEntity.ok(response);
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
    
    /**
     * 解析文件URL到实际文件路径
     * 使用jar所在目录作为基准目录
     * @param fileUrl 文件相对URL，如 "/uploads/homework/xxx.docx"
     * @return 实际文件路径
     */
    private Path resolveFilePath(String fileUrl) {
        // 去掉开头的斜杠，转换为相对路径
        String sanitizedPath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        
        // 获取jar所在目录作为基准
        ApplicationHome home = new ApplicationHome(getClass());
        File jarDir = home.getDir();
        
        // 构建完整路径
        Path fullPath = new File(jarDir, sanitizedPath).toPath();
        
        log.debug("文件路径解析: URL={}, jar目录={}, 完整路径={}", 
                fileUrl, jarDir.getAbsolutePath(), fullPath.toAbsolutePath());
        
        return fullPath;
    }
    
}