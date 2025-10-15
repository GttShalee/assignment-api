package cn.shalee.workupload.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应 DTO
 * @author 31930
 */
@Data
@Builder
public class UserInfoResponse {
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像地址
     */
    private String avatar_url;
    
    /**
     * 学号
     */
    private String studentId;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 班级代码
     */
    private String classCode;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 角色类型 (1-学生, 2-学委, 3-教师, 4-管理员)
     */
    private Integer roleType;
    
    /**
     * 账户状态
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 用户昵称
     */
    private String nickname;
} 