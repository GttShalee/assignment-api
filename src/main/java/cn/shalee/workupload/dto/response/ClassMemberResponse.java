package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班级成员响应DTO
 * @author 31930
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberResponse {
    
    private Long id;
    
    @JsonProperty("student_id")
    private String studentId;
    
    @JsonProperty("real_name")
    private String realName;
    
    private String email;
    
    @JsonProperty("role_type")
    private Integer roleType; // 0-管理员, 1-学生, 2-学委
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
} 