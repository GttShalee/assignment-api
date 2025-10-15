package cn.shalee.workupload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author shalee
 */
@Data
@Builder
public class LoginResponse {
    private String token;
    private String studentId;
    private Long userId;
    private String email;
    private String realName;
    private Integer roleType;
    private LocalDateTime expireTime;
    private Integer courses;
    
    @JsonProperty("nick_name")
    private String nickname;
}