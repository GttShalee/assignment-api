package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author shalee
 */
@Data
public class LoginRequest {
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "学号格式不正确")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20位之间")
    private String password;
}