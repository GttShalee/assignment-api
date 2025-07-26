package cn.shalee.workupload.security;

import cn.shalee.workupload.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 这里假设roleType为1-学生,2-学委,3-老师,4-管理员
        String role = "ROLE_USER";
        if (user.getRoleType() != null) {
            switch (user.getRoleType()) {
                case 2: role = "ROLE_MONITOR"; break;
                case 3: role = "ROLE_TEACHER"; break;
                case 4: role = "ROLE_ADMIN"; break;
                default: role = "ROLE_USER";
            }
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null ? user.getStatus() : true;
    }

    public User getUser() {
        return user;
    }
}
