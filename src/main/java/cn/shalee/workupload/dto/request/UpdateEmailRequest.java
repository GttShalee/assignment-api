package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新邮箱请求 DTO
 * @author 31930
 */
@Data
public class UpdateEmailRequest {
    @NotBlank(message = "新邮箱不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式不正确")
    private String newEmail;
}
