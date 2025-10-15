package cn.shalee.workupload.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布帖子请求DTO
 * @author 31930
 */
@Data
public class CreateForumPostRequest {
    
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title; // 主帖必填，回复可为空
    
    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;
    
    @JsonProperty("parent_id")
    private Long parentId; // 回复时需要传入父帖子ID，发主帖时为null
    
    @JsonProperty("attachment_url")
    private String attachmentUrl; // 附件URL（可选）
    
    @JsonProperty("attachment_name")
    private String attachmentName; // 附件名称（可选）
}

