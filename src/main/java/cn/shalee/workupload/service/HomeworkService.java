package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.CreateHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.repository.HomeworkRepository;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkService {
    
    private final HomeworkRepository homeworkRepository;
    private final UserRepository userRepository;
    
    public HomeworkResponse createHomework(CreateHomeworkRequest request) {
        log.info("创建作业: title={}, classCode={}", request.getTitle(), request.getClassCode());
        
        Homework homework = Homework.builder()
                .classCode(request.getClassCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .attachmentUrl(request.getAttachmentUrl())
                .fileName(request.getFileName())
                .publishTime(request.getPublishTime())
                .deadline(request.getDeadline())
                .totalScore(request.getTotalScore())
                .status(request.getStatus())
                .build();
        
        Homework savedHomework = homeworkRepository.save(homework);
        log.info("作业创建成功: id={}", savedHomework.getId());
        
        return convertToResponse(savedHomework);
    }
    
    public Page<HomeworkResponse> getHomeworkListByUser(String userEmail, int page, int pageSize) {
        log.info("根据用户获取作业列表: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        log.info("用户信息: roleType={}, classCode={}", user.getRoleType(), user.getClassCode());
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Homework> homeworkPage;
        
        // 根据用户角色类型过滤作业
        if (user.getRoleType() == 0) {
            // role_type为0的返回所有作业
            log.info("用户角色为管理员，返回所有作业");
            homeworkPage = homeworkRepository.findAll(pageable);
        } else {
            // role_type为1或2的返回用户所在班级的作业
            if (user.getClassCode() == null || user.getClassCode().trim().isEmpty()) {
                log.warn("用户班级代码为空，返回空结果");
                homeworkPage = homeworkRepository.findByClassCode("", pageable); // 返回空结果
            } else {
                log.info("用户角色为学生/学委，返回班级作业: classCode={}", user.getClassCode());
                homeworkPage = homeworkRepository.findByClassCode(user.getClassCode(), pageable);
            }
        }
        
        return homeworkPage.map(this::convertToResponse);
    }
    
    public Page<HomeworkResponse> getHomeworkList(String classCode, Integer status, int page, int pageSize) {
        log.info("获取作业列表: classCode={}, status={}, page={}, pageSize={}", classCode, status, page, pageSize);
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Homework> homeworkPage;
        
        if (classCode != null && status != null) {
            homeworkPage = homeworkRepository.findByClassCodeAndStatus(classCode, status, pageable);
        } else if (classCode != null) {
            homeworkPage = homeworkRepository.findByClassCode(classCode, pageable);
        } else if (status != null) {
            homeworkPage = homeworkRepository.findByStatus(status, pageable);
        } else {
            homeworkPage = homeworkRepository.findAll(pageable);
        }
        
        return homeworkPage.map(this::convertToResponse);
    }
    
    public HomeworkResponse getHomeworkDetail(Long id) {
        log.info("获取作业详情: id={}", id);
        
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        return convertToResponse(homework);
    }
    
    public HomeworkResponse updateHomework(Long id, CreateHomeworkRequest request, String userEmail) {
        log.info("更新作业: id={}, userEmail={}", id, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 获取作业信息
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        log.info("用户信息: roleType={}, classCode={}", user.getRoleType(), user.getClassCode());
        log.info("作业信息: classCode={}", homework.getClassCode());
        
        // 权限检查
        if (user.getRoleType() == 0) {
            // role_type为0的管理员可以编辑任意作业
            log.info("管理员用户，允许编辑作业");
        } else if (user.getRoleType() == 2) {
            // role_type为2的学委只能编辑本班级的作业
            if (user.getClassCode() == null || user.getClassCode().trim().isEmpty()) {
                throw new BusinessException("PERMISSION-001", "用户班级代码为空，无法编辑作业");
            }
            if (!user.getClassCode().equals(homework.getClassCode())) {
                throw new BusinessException("PERMISSION-002", "只能编辑本班级的作业");
            }
            log.info("学委用户，编辑本班级作业");
        } else {
            // role_type为1的学生不能编辑作业
            throw new BusinessException("PERMISSION-003", "学生用户无权编辑作业");
        }
        
        // 更新作业信息 - 只更新非空字段，避免将必填字段设置为null
        if (request.getClassCode() != null && !request.getClassCode().trim().isEmpty()) {
            homework.setClassCode(request.getClassCode());
        }
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            homework.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            homework.setDescription(request.getDescription());
        }
        if (request.getAttachmentUrl() != null) {
            homework.setAttachmentUrl(request.getAttachmentUrl());
        }
        if (request.getFileName() != null) {
            homework.setFileName(request.getFileName());
        }
        if (request.getPublishTime() != null) {
            homework.setPublishTime(request.getPublishTime());
        }
        if (request.getDeadline() != null) {
            homework.setDeadline(request.getDeadline());
        }
        if (request.getTotalScore() != null) {
            homework.setTotalScore(request.getTotalScore());
        }
        if (request.getStatus() != null) {
            homework.setStatus(request.getStatus());
        }
        
        Homework updatedHomework = homeworkRepository.save(homework);
        log.info("作业更新成功: id={}", updatedHomework.getId());
        
        return convertToResponse(updatedHomework);
    }
    
    public void deleteHomework(Long id, String userEmail) {
        log.info("删除作业: id={}, userEmail={}", id, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 获取作业信息
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        log.info("用户信息: roleType={}, classCode={}", user.getRoleType(), user.getClassCode());
        log.info("作业信息: classCode={}", homework.getClassCode());
        
        // 权限检查
        if (user.getRoleType() == 0) {
            // role_type为0的管理员可以删除任意作业
            log.info("管理员用户，允许删除作业");
        } else if (user.getRoleType() == 2) {
            // role_type为2的学委只能删除本班级的作业
            if (user.getClassCode() == null || user.getClassCode().trim().isEmpty()) {
                throw new BusinessException("PERMISSION-001", "用户班级代码为空，无法删除作业");
            }
            if (!user.getClassCode().equals(homework.getClassCode())) {
                throw new BusinessException("PERMISSION-002", "只能删除本班级的作业");
            }
            log.info("学委用户，删除本班级作业");
        } else {
            // role_type为1的学生不能删除作业
            throw new BusinessException("PERMISSION-003", "学生用户无权删除作业");
        }
        
        homeworkRepository.deleteById(id);
        log.info("作业删除成功: id={}", id);
    }
    
    private HomeworkResponse convertToResponse(Homework homework) {
        return HomeworkResponse.builder()
                .id(homework.getId())
                .classCode(homework.getClassCode())
                .title(homework.getTitle())
                .description(homework.getDescription())
                .attachmentUrl(homework.getAttachmentUrl())
                .fileName(homework.getFileName())
                .publishTime(homework.getPublishTime())
                .deadline(homework.getDeadline())
                .totalScore(homework.getTotalScore())
                .status(homework.getStatus())
                .createdAt(homework.getCreatedAt())
                .updatedAt(homework.getUpdatedAt())
                .build();
    }
} 