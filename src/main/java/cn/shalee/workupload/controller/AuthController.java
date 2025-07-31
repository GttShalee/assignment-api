package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.EmailLoginRequest;
import cn.shalee.workupload.dto.request.LoginRequest;
import cn.shalee.workupload.dto.request.RegisterRequest;
import cn.shalee.workupload.dto.response.LoginResponse;
import cn.shalee.workupload.dto.response.UserInfoResponse;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.service.AuthService;
import cn.shalee.workupload.service.EmailService;
import cn.shalee.workupload.service.TokenValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final TokenValidationService tokenValidationService;
    
    // 内存验证码存储 - 改为非 final，避免构造器注入问题
    private final Map<String, String> codeMap = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body("邮箱和验证码不能为空");
        }

        String redisKey = "auth:code:" + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if (savedCode != null && savedCode.equals(code)) {
            // 验证成功后，可以选择立即删除验证码，或在注册/登录成功后再删除
            // redisTemplate.delete(redisKey); 
            return ResponseEntity.ok("验证码正确");
        } else {
            return ResponseEntity.badRequest().body("验证码错误或已过期");
        }
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        // 修正后的正则表达式，移除了两端多余的引号
        String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        if (email == null || !email.matches(regex)) {
            return ResponseEntity.badRequest().body("邮箱格式不正确");
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        codeMap.put(email, code);

        emailService.sendVerificationCode(email, code);
        // 存储到Redis，设置5分钟过期
        redisTemplate.opsForValue().set(
                "auth:code:" + email,
                code,
                5, TimeUnit.MINUTES
        );

        return ResponseEntity.ok("验证码已发送");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求: email={}, studentId={}", request.getEmail(), request.getStudentId());
        
        // 验证邮箱验证码
        String redisKey = "auth:code:" + request.getEmail();
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if (savedCode == null || !savedCode.equals(request.getVerificationCode())) {
            return ResponseEntity.status(401).body("验证码错误或已过期");
        }

        // 2. 检查邮箱是否已注册
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("该邮箱已被注册");
        }

        // 3. 检查学号是否已使用
        if (userRepository.existsByStudentId(request.getStudentId())) {
            return ResponseEntity.badRequest().body("该学号已被使用");
        }

        // 4. 创建用户实体
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setStudentId(request.getStudentId());
        user.setClassCode(request.getClassCode());

        user.setAvatarUrl("https://raw.githubusercontent.com/GttShalee/Blog-pic/main/default_avatar.png");
        // 默认学生角色
        user.setRoleType(1);
        // 默认激活状态
        user.setStatus(true);

        // 5. 保存到数据库
        userRepository.save(user);

        // 6. 移除已使用的验证码
        redisTemplate.delete(redisKey);
        log.info("用户注册成功: email={}", request.getEmail());
        return ResponseEntity.ok("注册成功");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到学号登录请求: studentId={}", request.getStudentId());
        try {
            LoginResponse response = authService.login(request);
            log.info("学号登录成功: studentId={}", request.getStudentId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("学号登录失败: studentId={}, error={}", request.getStudentId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/loginEmail")
    public ResponseEntity<LoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request) {
        log.info("收到邮箱登录请求: email={}", request.getEmail());
        try {
            LoginResponse response = authService.loginByEmail(request);
            log.info("邮箱登录成功: email={}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("邮箱登录失败: email={}, error={}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        try {
            // 从 SecurityContext 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(null);
            }

            // 获取用户邮箱（JWT 中的 subject）
            String email = authentication.getName();
            log.info("获取当前用户信息: email={}", email);

            // 从数据库获取完整用户信息
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 构建用户信息响应
            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(user.getId().toString())
                    .realName(user.getRealName())
                    .avatar_url(user.getAvatarUrl())
                    .studentId(user.getStudentId())
                    .email(user.getEmail())
                    .classCode(user.getClassCode())
                    .role(user.getRole())
                    .roleType(user.getRoleType())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();

            log.info("成功获取用户信息: userId={}, realName={}", user.getId(), user.getRealName());
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        try {
            // 从 SecurityContext 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok("已登出");
            }

            // 获取用户邮箱
            String email = authentication.getName();
            log.info("用户登出: email={}", email);
            
            // 清除用户的token ID
            tokenValidationService.clearUserTokenId(email);
            
            // 清除SecurityContext
            SecurityContextHolder.clearContext();
            
            log.info("用户登出成功: email={}", email);
            return ResponseEntity.ok("登出成功");
            
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("登出失败");
        }
    }

    /**
     * 更换头像
     */
    @PostMapping("/change_avatar")
    public ResponseEntity<String> changeAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 从 SecurityContext 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("未登录");
            }

            // 获取用户邮箱
            String email = authentication.getName();
            log.info("用户更换头像: email={}", email);

            // 从数据库获取用户信息
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 调用服务层处理头像上传
            String avatarUrl = authService.changeAvatar(user, file);
            
            log.info("用户头像更换成功: email={}, avatarUrl={}", email, avatarUrl);
            return ResponseEntity.ok(avatarUrl);
            
        } catch (Exception e) {
            log.error("更换头像失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("更换头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新密码
     */
    @PostMapping("/change_passwd")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request) {
        try {
            // 从 SecurityContext 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("未登录");
            }

            // 获取用户邮箱
            String email = authentication.getName();
            log.info("用户更新密码: email={}", email);

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            String verificationCode = request.get("verificationCode");

            if (oldPassword == null || newPassword == null || verificationCode == null) {
                return ResponseEntity.badRequest().body("参数不完整");
            }

            // 调用服务层处理密码更新
            authService.changePassword(email, oldPassword, newPassword, verificationCode);
            
            log.info("用户密码更新成功: email={}", email);
            return ResponseEntity.ok("密码更新成功");
            
        } catch (Exception e) {
            log.error("更新密码失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("更新密码失败: " + e.getMessage());
        }
    }
}