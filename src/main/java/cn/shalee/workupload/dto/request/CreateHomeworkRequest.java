package cn.shalee.workupload.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author shalee
 */
@Data
public class CreateHomeworkRequest {
    
    @NotBlank(message = "班级代码不能为空")
    @JsonProperty("class_code")
    private String classCode;
    
    @JsonProperty("course_name")
    private String courseName;
    
    @NotBlank(message = "作业标题不能为空")
    private String title;
    
    private String description;
    
    @JsonProperty("attachment_url")
    private String attachmentUrl;
    
    @JsonProperty("file_name")
    private String fileName;
    
    @NotNull(message = "发布时间不能为空")
    @JsonProperty("publish_time")
    private LocalDateTime publishTime;
    
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    
    @NotNull(message = "总分不能为空")
    @Min(value = 0, message = "总分不能小于0")
    @JsonProperty("total_score")
    private Integer totalScore;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
} 