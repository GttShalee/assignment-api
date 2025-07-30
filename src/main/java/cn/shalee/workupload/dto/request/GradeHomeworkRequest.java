package cn.shalee.workupload.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批改作业请求DTO
 * @author 31930
 */
@Data
public class GradeHomeworkRequest {
    
    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.0", message = "分数不能小于0")
    @DecimalMax(value = "100.0", message = "分数不能大于100")
    @JsonProperty("score")
    private BigDecimal score;
    
    @JsonProperty("feedback")
    private String feedback;
} 