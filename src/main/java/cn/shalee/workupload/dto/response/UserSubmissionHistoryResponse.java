package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户历史提交记录响应DTO
 * @author 31930
 */
@Data
@Builder
public class UserSubmissionHistoryResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("homework_id")
    private Long homeworkId;
    
    @JsonProperty("homework_title")
    private String homeworkTitle;
    
    @JsonProperty("course_name")
    private String courseName;
    
    @JsonProperty("submission_time")
    private LocalDateTime submissionTime;
    
    @JsonProperty("submission_file_url")
    private String submissionFileUrl;
    
    @JsonProperty("submission_file_name")
    private String submissionFileName;
    
    @JsonProperty("download_url")
    private String downloadUrl;
    
    @JsonProperty("submission_status")
    private Integer submissionStatus; // 0-按时提交，1-补交
}

