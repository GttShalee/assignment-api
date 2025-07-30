package cn.shalee.workupload.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交作业请求DTO
 * @author 31930
 */
@Data
public class SubmitHomeworkRequest {
    
    @NotNull(message = "作业ID不能为空")
    @JsonProperty("homework_id")
    private Long homeworkId;
    
    @JsonProperty("submission_file_url")
    private String submissionFileUrl;
    
    @JsonProperty("submission_file_name")
    private String submissionFileName;
    
    @JsonProperty("remarks")
    private String remarks;
} 