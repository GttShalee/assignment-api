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
    
    @PostMapping
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(UPLOAD_DIR);
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
            
            String url = "/uploads/" + filename;
            
            UploadResponse response = new UploadResponse(url, filename);
            log.info("文件上传成功: url={}", url);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    public static class UploadResponse {
        private String url;
        private String filename;
        
        public UploadResponse(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }
        
        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
} 