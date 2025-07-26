package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
/**
 * @author Shalee
 */
// RegisterRequest.java
@Data
public class RegisterRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "验证码必须是6位数字")
    private String verificationCode;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20位之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "姓名必须是2-10个中文字符")
    private String realName;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "学号必须是8-20位字母或数字")
    private String studentId;

    @Pattern(regexp = "^[A-Za-z0-9]{4,10}$", message = "班级代码必须是4-10位字母或数字")
    private String classCode;
}