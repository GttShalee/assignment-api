package cn.shalee.workupload.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户选课请求
 * @author shalee
 */
@Data
public class UpdateCoursesRequest {
    
    @NotNull(message = "课程代码不能为空")
    private Integer courses; // 用户选择的课程代码总和（位掩码）
}
