package cn.shalee.workupload.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import cn.shalee.workupload.util.StoragePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    
    private static final String UPLOAD_DIR = "uploads/";
    
    /**
     * 上传学委发布的作业附件
     */
    @PostMapping("/homework-attachment")
    public ResponseEntity<UploadResponse> uploadHomeworkAttachment(@RequestParam("file") MultipartFile file) {
        log.info("收到作业附件上传请求: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 创建作业附件目录
            Path uploadPath = StoragePaths.getUploadsBasePath().resolve("homework-attachments");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/homework-attachments/" + filename;
            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(url)
                    .toUriString();
            
            UploadResponse response = new UploadResponse(url, fullUrl, filename, originalFilename);
            log.info("作业附件上传成功: url={}", url);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("作业附件上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 上传学生提交的作业文件
     */
    @PostMapping("/student-submission")
    public ResponseEntity<UploadResponse> uploadStudentSubmission(@RequestParam("file") MultipartFile file) {
        log.info("收到学生作业提交请求: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 创建学生作业提交目录
            Path uploadPath = StoragePaths.getUploadsBasePath().resolve("student-submissions");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/student-submissions/" + filename;
            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(url)
                    .toUriString();
            
            UploadResponse response = new UploadResponse(url, fullUrl, filename, originalFilename);
            log.info("学生作业提交成功: url={}", url);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("学生作业提交失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 通用文件上传（保持向后兼容）
     */
    @PostMapping
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到通用文件上传请求: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 确保上传目录存在
            Path uploadPath = StoragePaths.getUploadsBasePath().resolve("general");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/general/" + filename;
            String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(url)
                    .toUriString();
            
            UploadResponse response = new UploadResponse(url, fullUrl, filename, originalFilename);
            log.info("通用文件上传成功: url={}", url);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("通用文件上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    public static class UploadResponse {
        private String url;           // 相对路径，如 /uploads/... 
        private String fullUrl;       // 绝对地址，如 http://host:port/uploads/...
        private String filename;      // 存储文件名
        private String originalFilename; // 原始文件名

        public UploadResponse(String url, String fullUrl, String filename, String originalFilename) {
            this.url = url;
            this.fullUrl = fullUrl;
            this.filename = filename;
            this.originalFilename = originalFilename;
        }

        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFullUrl() { return fullUrl; }
        public void setFullUrl(String fullUrl) { this.fullUrl = fullUrl; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    }

    /**
     * 通用下载接口：通过相对路径下载文件，避免前端直接拼接端口
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("path") String relativePath,
                                           @RequestParam(value = "downloadName", required = false) String downloadName) {
        try {
            if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
                return ResponseEntity.badRequest().build();
            }
            // 统一去掉开头的斜杠
            String sanitized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            if (!sanitized.startsWith("uploads/")) {
                return ResponseEntity.badRequest().build();
            }
            // Always resolve under uploads base to prevent path traversal and cwd mismatch
            Path inRequest = Paths.get(sanitized);
            Path sub = inRequest.getNameCount() > 1 ? inRequest.subpath(1, inRequest.getNameCount()) : inRequest.getFileName();
            Path filePath = StoragePaths.getUploadsBasePath().resolve(sub);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            String fileName = downloadName != null && !downloadName.isBlank()
                    ? downloadName
                    : filePath.getFileName().toString();

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(Files.size(filePath));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Files.readAllBytes(filePath));
        } catch (IOException e) {
            log.error("文件下载失败: path={}", relativePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 