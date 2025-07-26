package cn.shalee.workupload.service.impl;

import cn.shalee.workupload.dto.request.EmailLoginRequest;
import cn.shalee.workupload.dto.request.LoginRequest;
import cn.shalee.workupload.dto.request.RegisterRequest;
import cn.shalee.workupload.dto.response.LoginResponse;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.security.JwtTokenProvider;
import cn.shalee.workupload.service.AuthService;
import cn.shalee.workupload.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author 31930
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeService verificationCodeService;

    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new BusinessException("AUTH-001", "学号不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("AUTH-002", "密码错误");
        }

        return generateLoginResponse(user);
    }

    private LoginResponse generateLoginResponse(User user) {
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(user);

        // 构建登录响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .realName(user.getRealName())
                .studentId(user.getStudentId())
                .roleType(user.getRoleType())
                .expireTime(LocalDateTime.now().plusHours(2)) // 假设token有效期2小时
                .build();
    }

    @Override
    public LoginResponse loginByEmail(EmailLoginRequest request) {
        // log.debug("进入了control层");
        // 1.验证邮箱格式
        if (!request.getEmail().matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")) {
            throw new BusinessException("AUTH-003", "邮箱格式不正确");
        }
        // 2.查看邮箱是否存在
        // 2. 从数据库获取用户
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("AUTH-001", "邮箱未注册"));

        // 3. 验证验证码
        if (!verificationCodeService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new BusinessException("AUTH-002", "验证码错误或已过期");
        }
        // 4. 生成JWT令牌
        return generateLoginResponse(user);
    }
}
