package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.GradeHomeworkRequest;
import cn.shalee.workupload.dto.request.SubmitHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkSubmissionResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.HomeworkLog;
import cn.shalee.workupload.entity.HomeworkSubmission;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.HomeworkLogRepository;
import cn.shalee.workupload.repository.HomeworkRepository;
import cn.shalee.workupload.repository.HomeworkSubmissionRepository;
import cn.shalee.workupload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 作业提交业务逻辑服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkSubmissionService {
    
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final HomeworkRepository homeworkRepository;
    private final UserRepository userRepository;
    private final HomeworkLogRepository homeworkLogRepository;
    
    /**
     * 提交作业
     */
    public HomeworkSubmissionResponse submitHomework(SubmitHomeworkRequest request, String userEmail) {
        log.info("学生提交作业: userEmail={}, homeworkId={}", userEmail, request.getHomeworkId());
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
//        // 检查用户权限（只有学生可以提交作业）
//        if (user.getRoleType() != 1) {
//            throw new BusinessException("PERMISSION-001", "只有学生可以提交作业");
//        }
//
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(request.getHomeworkId())
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 移除重复提交检查，让数据库唯一约束处理
        // 前端会根据homework_log表的status来控制是否显示提交按钮
        
        // 判断提交状态（按时提交还是补交）
        LocalDateTime now = LocalDateTime.now();
        Integer submissionStatus = now.isBefore(homework.getDeadline()) ? 0 : 1; // 0-按时提交，1-补交
        
        // 创建提交记录（检查是否已存在）
        Optional<HomeworkSubmission> existingSubmission = homeworkSubmissionRepository.findByStudentIdAndHomeworkId(user.getStudentId(), request.getHomeworkId());
        HomeworkSubmission savedSubmission;
        
        if (existingSubmission.isPresent()) {
            // 更新现有提交记录
            HomeworkSubmission submission = existingSubmission.get();
            submission.setSubmissionTime(now);
            submission.setSubmissionFileUrl(request.getSubmissionFileUrl());
            submission.setSubmissionFileName(request.getSubmissionFileName());
            submission.setSubmissionStatus(submissionStatus);
            submission.setRemarks(request.getRemarks());
            submission.setUpdatedAt(now);
            savedSubmission = homeworkSubmissionRepository.save(submission);
            log.info("更新作业提交记录: submissionId={}, studentId={}", savedSubmission.getId(), user.getStudentId());
        } else {
            // 创建新提交记录
            HomeworkSubmission submission = HomeworkSubmission.builder()
                    .studentId(user.getStudentId())
                    .classCode(user.getClassCode())
                    .homeworkId(request.getHomeworkId())
                    .submissionTime(now)
                    .submissionFileUrl(request.getSubmissionFileUrl())
                    .submissionFileName(request.getSubmissionFileName())
                    .submissionStatus(submissionStatus)
                    .remarks(request.getRemarks())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            savedSubmission = homeworkSubmissionRepository.save(submission);
            log.info("创建作业提交记录: submissionId={}, studentId={}", savedSubmission.getId(), user.getStudentId());
        }
        
        // 记录作业提交日志（使用saveOrUpdate逻辑）
        Optional<HomeworkLog> existingLog = homeworkLogRepository.findByHomeworkIdAndStudentId(request.getHomeworkId().intValue(), user.getStudentId());
        HomeworkLog homeworkLog;
        
        if (existingLog.isPresent()) {
            // 更新现有记录
            homeworkLog = existingLog.get();
            homeworkLog.setStatus(1); // 1表示已提交
            homeworkLog.setUpdatedAt(LocalDateTime.now());
            log.info("更新作业提交日志: homeworkId={}, studentId={}", request.getHomeworkId(), user.getStudentId());
        } else {
            // 创建新记录
            homeworkLog = HomeworkLog.builder()
                    .homeworkId(request.getHomeworkId().intValue())
                    .studentId(user.getStudentId())
                    .status(1) // 1表示已提交
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            log.info("创建作业提交日志: homeworkId={}, studentId={}", request.getHomeworkId(), user.getStudentId());
        }
        
        homeworkLogRepository.save(homeworkLog);
        log.info("作业提交日志记录成功: homeworkId={}, studentId={}", request.getHomeworkId(), user.getStudentId());
        
        return convertToResponse(savedSubmission, user, homework);
    }
    
    /**
     * 获取我的作业提交列表
     */
    public Page<HomeworkSubmissionResponse> getMySubmissions(String userEmail, int page, int pageSize) {
        log.info("获取我的作业提交列表: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<HomeworkSubmission> submissionPage = homeworkSubmissionRepository.findByStudentId(user.getStudentId(), pageable);
        
        return submissionPage.map(submission -> {
            Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
            return convertToResponse(submission, user, homework);
        });
    }
    
    /**
     * 获取作业提交详情
     */
    public HomeworkSubmissionResponse getSubmissionDetail(Long submissionId, String userEmail) {
        log.info("获取作业提交详情: submissionId={}, userEmail={}", submissionId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 获取提交记录
        HomeworkSubmission submission = homeworkSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("SUBMISSION-002", "提交记录不存在"));
        
        // 权限检查：只能查看自己的提交记录，或者学委/管理员可以查看
        if (user.getRoleType() == 1 && !user.getStudentId().equals(submission.getStudentId())) {
            throw new BusinessException("PERMISSION-002", "无权查看他人的提交记录");
        }
        
        Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
        return convertToResponse(submission, user, homework);
    }
    
    /**
     * 获取作业的所有提交记录（学委/管理员）
     */
    public Page<HomeworkSubmissionResponse> getHomeworkSubmissions(Long homeworkId, String userEmail, int page, int pageSize) {
        log.info("获取作业提交记录: homeworkId={}, userEmail={}, page={}, pageSize={}", homeworkId, userEmail, page, pageSize);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 权限检查：只有学委和管理员可以查看
        if (user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("PERMISSION-003", "只有学委和管理员可以查看作业提交记录");
        }
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 如果是学委，只能查看本班级的提交记录
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<HomeworkSubmission> submissionPage;
        
        if (user.getRoleType() == 2) {
            submissionPage = homeworkSubmissionRepository.findByHomeworkIdAndClassCode(homeworkId, user.getClassCode(), pageable);
        } else {
            submissionPage = homeworkSubmissionRepository.findByHomeworkId(homeworkId, pageable);
        }
        
        return submissionPage.map(submission -> {
            User student = userRepository.findByStudentId(submission.getStudentId()).orElse(null);
            return convertToResponse(submission, student, homework);
        });
    }
    
    /**
     * 批改作业（学委/管理员）
     */
    public HomeworkSubmissionResponse gradeHomework(Long submissionId, GradeHomeworkRequest request, String userEmail) {
        log.info("批改作业: submissionId={}, userEmail={}, score={}", submissionId, userEmail, request.getScore());
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 权限检查：只有学委和管理员可以批改
        if (user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("PERMISSION-004", "只有学委和管理员可以批改作业");
        }
        
        // 获取提交记录
        HomeworkSubmission submission = homeworkSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("SUBMISSION-002", "提交记录不存在"));
        
        // 如果是学委，只能批改本班级的作业
        if (user.getRoleType() == 2 && !user.getClassCode().equals(submission.getClassCode())) {
            throw new BusinessException("PERMISSION-005", "只能批改本班级的作业");
        }
        
        // 更新分数和评语
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setUpdatedAt(LocalDateTime.now());
        
        HomeworkSubmission updatedSubmission = homeworkSubmissionRepository.save(submission);
        log.info("作业批改成功: submissionId={}, score={}", submissionId, request.getScore());
        
        User student = userRepository.findByStudentId(submission.getStudentId()).orElse(null);
        Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
        return convertToResponse(updatedSubmission, student, homework);
    }
    
    /**
     * 获取班级作业统计信息
     */
    public Object getHomeworkStats(Long homeworkId, String userEmail) {
        log.info("获取作业统计信息: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 权限检查：只有学委和管理员可以查看统计
        if (user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("PERMISSION-006", "只有学委和管理员可以查看统计信息");
        }
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 获取统计信息
        long totalSubmissions;
        long onTimeSubmissions;
        long lateSubmissions;

        if (user.getRoleType() == 2) {
            // 学委只能查看本班级统计
            totalSubmissions = homeworkSubmissionRepository.countByHomeworkIdAndClassCode(homeworkId, user.getClassCode());
            onTimeSubmissions = homeworkSubmissionRepository.findByHomeworkIdAndSubmissionStatus(homeworkId, 0).size();
            lateSubmissions = homeworkSubmissionRepository.findByHomeworkIdAndSubmissionStatus(homeworkId, 1).size();
        } else {
            // 管理员查看所有统计
            totalSubmissions = homeworkSubmissionRepository.countByHomeworkId(homeworkId);
            onTimeSubmissions = homeworkSubmissionRepository.findByHomeworkIdAndSubmissionStatus(homeworkId, 0).size();
            lateSubmissions = homeworkSubmissionRepository.findByHomeworkIdAndSubmissionStatus(homeworkId, 1).size();
        }

        return new Object() {
            public final long total = totalSubmissions;
            public final long onTime = onTimeSubmissions;
            public final long late = lateSubmissions;
            public final long graded = totalSubmissions; // 简化处理，实际应该统计已批改的数量
        };
    }
    
    /**
     * 根据作业ID获取作业信息
     */
    public Homework getHomeworkById(Long homeworkId) {
        return homeworkRepository.findById(homeworkId).orElse(null);
    }
    
    /**
     * 根据邮箱获取用户信息
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * 下载作业提交包
     */
    public byte[] downloadHomeworkSubmissions(Long homeworkId, String userEmail) throws IOException {
        log.info("下载作业提交包: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 权限检查：只有学委和管理员可以下载
        if (user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("PERMISSION-007", "只有学委和管理员可以下载作业提交包");
        }
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 获取提交记录
        List<HomeworkSubmission> submissions;
        if (user.getRoleType() == 2) {
            // 学委只能下载本班级的提交
            submissions = homeworkSubmissionRepository.findByHomeworkIdAndClassCode(homeworkId, user.getClassCode());
        } else {
            // 管理员下载所有提交
            submissions = homeworkSubmissionRepository.findByHomeworkId(homeworkId);
        }
        
        if (submissions.isEmpty()) {
            throw new BusinessException("SUBMISSION-003", "没有找到作业提交记录");
        }
        
        // 创建临时ZIP文件
        Path tempZipPath = Files.createTempFile("homework_submissions_", ".zip");
        
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipPath))) {
            for (HomeworkSubmission submission : submissions) {
                if (submission.getSubmissionFileUrl() != null && !submission.getSubmissionFileUrl().isEmpty()) {
                    // 构建文件路径
                    String fileUrl = submission.getSubmissionFileUrl();
                    if (fileUrl.startsWith("/uploads/")) {
                        fileUrl = fileUrl.substring(1); // 去掉开头的斜杠
                    }
                    
                    Path filePath = Paths.get(fileUrl);
                    if (Files.exists(filePath)) {
                        // 获取学生信息
                        User student = userRepository.findByStudentId(submission.getStudentId()).orElse(null);
                        String studentName = student != null ? student.getRealName() : submission.getStudentId();
                        
                        // 构建ZIP中的文件名：学号_姓名_原始文件名
                        String originalFileName = submission.getSubmissionFileName();
                        String extension = "";
                        if (originalFileName != null && originalFileName.contains(".")) {
                            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        }
                        String zipFileName = submission.getStudentId() + "_" + studentName + extension;
                        
                        // 添加到ZIP文件
                        ZipEntry zipEntry = new ZipEntry(zipFileName);
                        zipOut.putNextEntry(zipEntry);
                        
                        Files.copy(filePath, zipOut);
                        zipOut.closeEntry();
                        
                        log.info("添加文件到ZIP: studentId={}, fileName={}", submission.getStudentId(), zipFileName);
                    } else {
                        log.warn("文件不存在: {}", filePath);
                    }
                }
            }
        }
        
        // 读取ZIP文件内容
        byte[] zipContent = Files.readAllBytes(tempZipPath);
        
        // 删除临时文件
        Files.delete(tempZipPath);
        
        log.info("作业提交包下载完成: homeworkId={}, fileCount={}, size={} bytes", 
                homeworkId, submissions.size(), zipContent.length);
        
        return zipContent;
    }
    
    /**
     * 获取作业提交包的文件名
     */
    public String getHomeworkSubmissionsZipFileName(Long homeworkId, String userEmail) {
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 生成文件名：作业提交_{作业标题}.zip
        String fileName = "作业提交_" + homework.getTitle() + ".zip";
        // 处理文件名中的特殊字符
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        return fileName;
    }
    
    /**
     * 转换为响应DTO
     */
    private HomeworkSubmissionResponse convertToResponse(HomeworkSubmission submission, User student, Homework homework) {
        return HomeworkSubmissionResponse.builder()
                .id(submission.getId())
                .studentId(submission.getStudentId())
                .classCode(submission.getClassCode())
                .homeworkId(submission.getHomeworkId())
                .submissionTime(submission.getSubmissionTime())
                .submissionFileUrl(submission.getSubmissionFileUrl())
                .submissionFileName(submission.getSubmissionFileName())
                .score(submission.getScore())
                .submissionStatus(submission.getSubmissionStatus())
                .remarks(submission.getRemarks())
                .feedback(submission.getFeedback())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .studentName(student != null ? student.getRealName() : null)
                .homeworkTitle(homework != null ? homework.getTitle() : null)
                .build();
    }
} 