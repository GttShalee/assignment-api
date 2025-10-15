package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子响应DTO
 * @author 31930
 */
@Data
@Builder
public class ForumPostResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("student_name")
    private String studentName; // 发帖人姓名
    
    @JsonProperty("student_avatar")
    private String studentAvatar; // 发帖人头像
    
    @JsonProperty("nickname")
    private String nickname; // 发帖人昵称
    
    @JsonProperty("class_code")
    private String classCode;
    
    @JsonProperty("parent_id")
    private Long parentId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("attachment_url")
    private String attachmentUrl;
    
    @JsonProperty("attachment_name")
    private String attachmentName;
    
    @JsonProperty("like_count")
    private Integer likeCount;
    
    @JsonProperty("reply_count")
    private Integer replyCount;
    
    @JsonProperty("view_count")
    private Integer viewCount;
    
    @JsonProperty("is_top")
    private Boolean isTop;
    
    @JsonProperty("is_hot")
    private Boolean isHot;
    
    @JsonProperty("is_liked")
    private Boolean isLiked; // 当前用户是否已点赞
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}


