package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 未交作业成员响应DTO
 * @author 31930
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnsubmittedMemberResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("real_name")
    private String realName;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    @JsonProperty("role_type")
    private Integer roleType;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
} 