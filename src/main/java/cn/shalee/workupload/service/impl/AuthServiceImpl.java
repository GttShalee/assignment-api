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
import cn.shalee.workupload.service.TokenValidationService;
import cn.shalee.workupload.service.VerificationCodeService;
import cn.shalee.workupload.util.StoragePaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * @author shalee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenValidationService tokenValidationService;
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
        
        // 获取token ID并更新用户记录
        String tokenId = jwtTokenProvider.getTokenIdFromToken(token);
        tokenValidationService.updateUserTokenId(user.getEmail(), tokenId);

        // 构建登录响应
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .realName(user.getRealName())
                .studentId(user.getStudentId())
                .roleType(user.getRoleType())
                .expireTime(LocalDateTime.now().plusHours(6)) // 假设token有效期2小时
                .courses(user.getCourses())
                .nickname(user.getNickname())
                .classCode(user.getClassCode())
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
    
    @Override
    public String changeAvatar(User user, MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException("AVATAR-001", "请选择头像文件");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("AVATAR-002", "只能上传图片文件");
        }
        
        // 验证文件大小（限制为5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("AVATAR-003", "头像文件大小不能超过5MB");
        }
        
        try {
            // 创建用户专属文件夹
            String userFolderName = user.getStudentId() + user.getRealName();
            Path uploadPath = StoragePaths.getUploadsBasePath().resolve("avatar").resolve(userFolderName);
            
            // 如果文件夹不存在，创建文件夹
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "avatar_" + System.currentTimeMillis() + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // 更新用户头像URL
            String avatarUrl = "/uploads/avatar/" + userFolderName + "/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            
            log.info("用户头像更新成功: userId={}, avatarUrl={}", user.getId(), avatarUrl);
            return avatarUrl;
            
        } catch (IOException e) {
            log.error("保存头像文件失败", e);
            throw new BusinessException("AVATAR-004", "保存头像文件失败");
        }
    }
    
    @Override
    public void changePassword(String email, String newPassword, String verificationCode) {
        // 1. 验证用户是否存在
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("AUTH-001", "用户不存在"));
        
//        // 2. 验证旧密码
//        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
//            throw new BusinessException("AUTH-002", "旧密码错误");
//        }

        // 3. 验证新密码格式（至少6位）
        if (newPassword.length() < 6) {
            throw new BusinessException("AUTH-003", "新密码长度不能少于6位");
        }
        
        // 4. 验证邮箱验证码
        if (!verificationCodeService.verifyCode(email, verificationCode)) {
            throw new BusinessException("AUTH-004", "验证码错误或已过期");
        }
        
        // 5. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码更新成功: email={}", email);
    }
    
    @Override
    public String updateEmail(String currentEmail, String newEmail) {
        // 1. 验证新邮箱格式
        if (!newEmail.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")) {
            throw new BusinessException("EMAIL-001", "邮箱格式不正确");
        }
        
        // 2. 验证用户是否存在
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new BusinessException("AUTH-001", "用户不存在"));
        
        // 3. 检查新邮箱是否已被其他用户使用
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException("EMAIL-002", "该邮箱已被其他用户使用");
        }
        
        // 4. 检查新邮箱是否与学号相同
        if (newEmail.equals(user.getStudentId())) {
            throw new BusinessException("EMAIL-003", "新邮箱不能与学号相同");
        }
        
        // 5. 更新用户邮箱
        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        userRepository.save(user);
        
        log.info("用户邮箱更新成功: userId={}, oldEmail={}, newEmail={}", user.getId(), oldEmail, newEmail);
        return newEmail;
    }
}
