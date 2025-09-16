package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.EmailLoginRequest;
import cn.shalee.workupload.dto.request.LoginRequest;
import cn.shalee.workupload.dto.request.RegisterRequest;
import cn.shalee.workupload.dto.response.LoginResponse;
import cn.shalee.workupload.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 31930
 */
public interface AuthService {
    /**
     * 用户注册
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    /**
     * @param request
     * @return
     */
    abstract LoginResponse login(LoginRequest request);

    /**
     * @param request
     * @return
     */
    abstract LoginResponse loginByEmail(EmailLoginRequest request);
    
    /**
     * 更换头像
     * @param user 用户信息
     * @param file 头像文件
     * @return 头像URL
     */
    String changeAvatar(User user, MultipartFile file);
    
    /**
     * 更新密码
     * @param email 用户邮箱
     * @param newPassword 新密码
     * @param verificationCode 验证码
     */
    void changePassword(String email, String newPassword, String verificationCode);
    
    /**
     * 更新邮箱
     * @param currentEmail 当前用户邮箱
     * @param newEmail 新邮箱
     * @return 更新后的邮箱
     */
    String updateEmail(String currentEmail, String newEmail);
}
