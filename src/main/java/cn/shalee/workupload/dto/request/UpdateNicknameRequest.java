package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新昵称请求DTO
 * @author 31930
 */
@Data
public class UpdateNicknameRequest {
    
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 50, message = "昵称长度必须在1-50个字符之间")
    private String nickname;
}

