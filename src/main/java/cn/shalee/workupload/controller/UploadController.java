package cn.shalee.workupload.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private static final String UPLOAD_DIR = "uploads/";
    
    /**
     * 上传学委发布的作业附件
     */
    @PostMapping("/homework-attachment")
    public ResponseEntity<UploadResponse> uploadHomeworkAttachment(@RequestParam("file") MultipartFile file) {
        log.info("收到作业附件上传请求: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 创建作业附件目录
            Path uploadPath = Paths.get(UPLOAD_DIR + "homework-attachments/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/homework-attachments/" + filename;
            
            UploadResponse response = new UploadResponse(url, filename, originalFilename);
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
            Path uploadPath = Paths.get(UPLOAD_DIR + "student-submissions/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/student-submissions/" + filename;
            
            UploadResponse response = new UploadResponse(url, filename, originalFilename);
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
            Path uploadPath = Paths.get(UPLOAD_DIR + "general/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            String url = "/uploads/general/" + filename;
            
            UploadResponse response = new UploadResponse(url, filename, originalFilename);
            log.info("通用文件上传成功: url={}", url);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("通用文件上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    public static class UploadResponse {
        private String url;
        private String filename;
        private String originalFilename;
        
        public UploadResponse(String url, String filename, String originalFilename) {
            this.url = url;
            this.filename = filename;
            this.originalFilename = originalFilename;
        }
        
        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    }
} 