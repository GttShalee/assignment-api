package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交记录响应DTO
 * @author 31930
 */
@Data
@Builder
public class SubmissionRecordResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("user_name")
    private String userName; // 用户名（真实姓名）
    
    @JsonProperty("homework_id")
    private Long homeworkId;
    
    @JsonProperty("homework_title")
    private String homeworkTitle; // 作业名称
    
    @JsonProperty("submission_time")
    private LocalDateTime submissionTime; // 提交时间
    
    @JsonProperty("submission_status")
    private Integer submissionStatus; // 0-按时提交，1-补交
    
    @JsonProperty("is_late_submission")
    private Boolean isLateSubmission; // 是否补交标记（true表示补交）
    
    @JsonProperty("is_first_submission")
    private Boolean isFirstSubmission; // 是否首位提交用户标记（true表示是首位）
    
    @JsonProperty("class_code")
    private String classCode;
    
    @JsonProperty("course_name")
    private String courseName;
}


