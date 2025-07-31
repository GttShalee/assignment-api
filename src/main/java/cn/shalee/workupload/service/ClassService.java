package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.response.ClassMemberResponse;
import cn.shalee.workupload.dto.response.ClassResponse;
import cn.shalee.workupload.entity.Class;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.ClassRepository;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级业务逻辑服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {
    
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final TokenValidationService tokenValidationService;
    
    /**
     * 获取班级列表
     */
    public List<ClassResponse> getClassList() {
        log.info("获取班级列表");
        
        List<Class> classes = classRepository.findAll();
        return classes.stream()
                .map(this::convertToClassResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取班级信息
     */
    public ClassResponse getClassInfo(String classCode) {
        log.info("获取班级信息: classCode={}", classCode);
        
        Class clazz = classRepository.findByClassCode(classCode)
                .orElseThrow(() -> new BusinessException("CLASS-001", "班级不存在"));
        
        return convertToClassResponse(clazz);
    }
    
    /**
     * 获取班级成员列表
     */
    public Page<ClassMemberResponse> getClassMembers(String classCode, int page, int pageSize) {
        log.info("获取班级成员列表: classCode={}, page={}, pageSize={}", classCode, page, pageSize);
        
        // 检查班级是否存在
        Class clazz = classRepository.findByClassCode(classCode)
                .orElseThrow(() -> new BusinessException("CLASS-001", "班级不存在"));
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<User> userPage = userRepository.findByClassCode(classCode, pageable);
        
        return userPage.map(this::convertToClassMemberResponse);
    }
    
    /**
     * 强制用户下线
     */
    public void forceUserLogout(String classCode, String studentId) {
        log.info("强制用户下线: classCode={}, studentId={}", classCode, studentId);
        
        // 检查班级是否存在
        Class clazz = classRepository.findByClassCode(classCode)
                .orElseThrow(() -> new BusinessException("CLASS-001", "班级不存在"));
        
        // 查找用户
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 检查用户是否属于该班级
        if (!classCode.equals(user.getClassCode())) {
            throw new BusinessException("PERMISSION-001", "用户不属于该班级");
        }
        
        // 强制用户下线
        tokenValidationService.forceUserLogout(user.getEmail());
        
        log.info("强制用户下线成功: classCode={}, studentId={}, email={}", 
                classCode, studentId, user.getEmail());
    }
    
    /**
     * 转换为班级响应DTO
     */
    private ClassResponse convertToClassResponse(Class clazz) {
        return ClassResponse.builder()
                .classCode(clazz.getClassCode())
                .className(clazz.getClassName())
                .build();
    }
    
    /**
     * 转换为班级成员响应DTO
     */
    private ClassMemberResponse convertToClassMemberResponse(User user) {
        return ClassMemberResponse.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .realName(user.getRealName())
                .email(user.getEmail())
                .roleType(user.getRoleType())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
} 