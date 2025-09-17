package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author shalee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkResponse {
    
    private Long id;
    @JsonProperty("class_code")
    private String classCode;
    @JsonProperty("course_name")
    private String courseName;
    private String title;
    private String description;
    @JsonProperty("attachment_url")
    private String attachmentUrl;
    @JsonProperty("publish_time")
    private LocalDateTime publishTime;
    private LocalDateTime deadline;
    @JsonProperty("total_score")
    private Integer totalScore;
    private Integer status;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("file_name")
    private String fileName;
    
    @JsonProperty("submission_status")
    private Integer submissionStatus; // 0-未提交，1-已提交
} 