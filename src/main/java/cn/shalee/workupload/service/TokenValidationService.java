package cn.shalee.workupload.service;

import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Token验证服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidationService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    /**
     * 验证token是否有效（包括单点登录检查）
     */
    public boolean validateToken(String token) {
        try {
            // 1. 验证JWT格式和签名
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("JWT token格式或签名无效");
                return false;
            }
            
            // 2. 获取用户信息
            String userEmail = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (user == null) {
                log.warn("Token中的用户不存在: email={}", userEmail);
                return false;
            }
            
            // 3. 检查用户状态
            if (!user.getStatus()) {
                log.warn("用户账户已被禁用: email={}", userEmail);
                return false;
            }
            
            // 4. 检查单点登录限制
            String tokenId = jwtTokenProvider.getTokenIdFromToken(token);
            if (tokenId == null) {
                log.warn("Token中缺少tokenId: email={}", userEmail);
                return false;
            }
            
            if (!tokenId.equals(user.getCurrentTokenId())) {
                log.warn("Token已失效（用户在其他设备登录）: email={}, currentTokenId={}, tokenId={}", 
                        userEmail, user.getCurrentTokenId(), tokenId);
                return false;
            }
            
            log.debug("Token验证成功: email={}", userEmail);
            return true;
            
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return false;
        }
    }
    
    /**
     * 更新用户的当前token ID（登录时调用）
     */
    public void updateUserTokenId(String userEmail, String tokenId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("AUTH-001", "用户不存在"));
        
        user.setCurrentTokenId(tokenId);
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        log.info("更新用户token ID: email={}, tokenId={}", userEmail, tokenId);
    }
    
    /**
     * 清除用户的token ID（登出时调用）
     */
    public void clearUserTokenId(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        
        if (user != null) {
            user.setCurrentTokenId(null);
            userRepository.save(user);
            log.info("清除用户token ID: email={}", userEmail);
        }
    }
    
    /**
     * 强制用户下线（管理员功能）
     */
    public void forceUserLogout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("AUTH-001", "用户不存在"));
        
        user.setCurrentTokenId(null);
        userRepository.save(user);
        
        log.info("强制用户下线: email={}", userEmail);
    }
} 