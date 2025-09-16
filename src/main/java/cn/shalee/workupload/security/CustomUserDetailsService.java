package cn.shalee.workupload.security;

import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String subjectValue) throws UsernameNotFoundException {
        // subjectValue可能是学号或邮箱（向后兼容）
        User user = null;
        
        // 先尝试作为学号查找
        user = userRepository.findByStudentId(subjectValue).orElse(null);
        
        // 如果找不到，尝试作为邮箱查找（向后兼容旧token）
        if (user == null) {
            user = userRepository.findByEmail(subjectValue).orElse(null);
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + subjectValue);
        }
        
        return new CustomUserDetails(user);
    }
} 