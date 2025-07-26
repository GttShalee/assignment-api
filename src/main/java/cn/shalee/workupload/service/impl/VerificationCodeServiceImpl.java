package cn.shalee.workupload.service.impl;

import cn.shalee.workupload.service.EmailService;
import cn.shalee.workupload.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void sendCode(String email) {
        // 1. 生成随机验证码
        String code = generateRandomCode();

        // 2. 存储到Redis (5分钟过期)
        redisTemplate.opsForValue().set(
                "auth:code:" + email,
                code,
                5, TimeUnit.MINUTES
        );

        // 3. 发送邮件
        emailService.sendVerificationCode(email, code);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        // 从Redis获取验证码
        String storedCode = redisTemplate.opsForValue().get("auth:code:" + email);

        // 验证成功后删除验证码
        if (code.equals(storedCode)) {
            redisTemplate.delete("auth:code:" + email);
            return true;
        }
        return false;
    }

    private String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}