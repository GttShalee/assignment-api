package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交响应DTO
 * @author 31930
 */
@Data
@Builder
public class HomeworkSubmissionResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("class_code")
    private String classCode;
    
    @JsonProperty("homework_id")
    private Long homeworkId;
    
    @JsonProperty("submission_time")
    private LocalDateTime submissionTime;
    
    @JsonProperty("submission_file_url")
    private String submissionFileUrl;
    
    @JsonProperty("submission_file_name")
    private String submissionFileName;
    
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("submission_status")
    private Integer submissionStatus; // 0-按时提交，1-补交
    
    @JsonProperty("remarks")
    private String remarks;
    
    @JsonProperty("feedback")
    private String feedback;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    // 扩展字段：学生信息
    @JsonProperty("student_name")
    private String studentName;
    
    @JsonProperty("homework_title")
    private String homeworkTitle;
} 