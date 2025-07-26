package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.EmailLoginRequest;
import cn.shalee.workupload.dto.request.LoginRequest;
import cn.shalee.workupload.dto.request.RegisterRequest;
import cn.shalee.workupload.dto.response.LoginResponse;

/**
 * @author 31930
 */
public interface AuthService {
    /**
     * 用户注册
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    abstract LoginResponse login(LoginRequest request);
    abstract LoginResponse loginByEmail(EmailLoginRequest request);
}
