package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 31930
 */
@Data
public class EmailLoginRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确1")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    private String verificationCode;
}