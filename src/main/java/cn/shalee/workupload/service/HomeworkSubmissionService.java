package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.GradeHomeworkRequest;
import cn.shalee.workupload.dto.request.SubmitHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkSubmissionResponse;
import cn.shalee.workupload.dto.response.SubmissionRecordResponse;
import cn.shalee.workupload.dto.response.UnsubmittedMemberResponse;
import cn.shalee.workupload.dto.response.UserSubmissionHistoryResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.entity.HomeworkLog;
import cn.shalee.workupload.entity.HomeworkSubmission;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.HomeworkLogRepository;
import cn.shalee.workupload.repository.HomeworkRepository;
import cn.shalee.workupload.repository.HomeworkSubmissionRepository;
import cn.shalee.workupload.repository.UserRepository;
import cn.shalee.workupload.util.StoragePaths;
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
import java.time.format.DateTimeFormatter;
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
        
        // 确保页码至少为1，然后转换为0基索引
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<HomeworkSubmission> submissionPage = homeworkSubmissionRepository.findByStudentId(user.getStudentId(), pageable);
        
        return submissionPage.map(submission -> {
            Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
            return convertToResponse(submission, user, homework);
        });
    }
    
    /**
     * 获取用户历史提交记录
     */
    public Page<UserSubmissionHistoryResponse> getUserSubmissionHistory(String userEmail, int page, int pageSize) {
        log.info("获取用户历史提交记录: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 确保页码至少为1，然后转换为0基索引
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<HomeworkSubmission> submissionPage = homeworkSubmissionRepository.findByStudentId(user.getStudentId(), pageable);
        
        return submissionPage.map(submission -> {
            // 获取作业信息以获取课程名称
            Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
            
            return UserSubmissionHistoryResponse.builder()
                    .id(submission.getId())
                    .homeworkId(submission.getHomeworkId())
                    .homeworkTitle(homework != null ? homework.getTitle() : "未知作业")
                    .courseName(homework != null ? homework.getCourseName() : "未知课程")
                    .submissionTime(submission.getSubmissionTime())
                    .submissionFileUrl(submission.getSubmissionFileUrl())
                    .submissionFileName(submission.getSubmissionFileName())
                    .downloadUrl("/api/homework-submission/download/" + submission.getId()) // 使用专用下载接口
                    .submissionStatus(submission.getSubmissionStatus())
                    .build();
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
        // 确保页码至少为1，然后转换为0基索引
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
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
     * 获取当前用户在指定作业下的提交记录
     */
    public HomeworkSubmissionResponse getMySubmissionByHomeworkId(Long homeworkId, String userEmail) {
        log.info("获取我的作业提交: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 查找用户的提交记录
        Optional<HomeworkSubmission> submissionOpt = homeworkSubmissionRepository.findByStudentIdAndHomeworkId(user.getStudentId(), homeworkId);
        
        if (submissionOpt.isEmpty()) {
            log.warn("用户未提交该作业: homeworkId={}, studentId={}", homeworkId, user.getStudentId());
            return null;
        }
        
        HomeworkSubmission submission = submissionOpt.get();
        return convertToResponse(submission, user, homework);
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
            // 直接遍历作业文件夹下的所有文件
            String timestamp = homework.getPublishTime().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String folderName = homework.getClassCode() + "-" + homework.getTitle() + "-" + timestamp;
            folderName = folderName.replaceAll("[\\\\/:*?\"<>|]", "_");
            
            Path homeworkFolder = StoragePaths.getUploadsBasePath().resolve("homework").resolve(folderName);
            
            if (Files.exists(homeworkFolder) && Files.isDirectory(homeworkFolder)) {
                try (var files = Files.list(homeworkFolder)) {
                    files.filter(Files::isRegularFile)
                         .forEach(filePath -> {
                             try {
                                 String fileName = filePath.getFileName().toString();
                                 ZipEntry zipEntry = new ZipEntry(fileName);
                                 zipOut.putNextEntry(zipEntry);
                                 Files.copy(filePath, zipOut);
                                 zipOut.closeEntry();
                                 log.info("添加文件到ZIP: fileName={}", fileName);
                             } catch (IOException e) {
                                 log.error("添加文件到ZIP失败: {}", filePath, e);
                             }
                         });
                }
            } else {
                log.warn("作业文件夹不存在: {}", homeworkFolder);
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
    
    /**
     * 获取作业未交成员列表
     */
    public List<UnsubmittedMemberResponse> getUnsubmittedMembers(Long homeworkId, String userEmail) {
        log.info("获取作业未交成员列表: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 检查权限（只有学委或管理员可以查看）
        if (user.getRoleType() != 2 && user.getRoleType() != 3) {
            throw new BusinessException("PERMISSION-001", "权限不足");
        }
        
        // 检查用户是否属于该班级
        if (!homework.getClassCode().equals(user.getClassCode())) {
            throw new BusinessException("PERMISSION-002", "只能查看本班级的作业");
        }
        
        // 获取班级所有成员
        List<User> classMembers = userRepository.findByClassCode(homework.getClassCode());
        
        // 获取所有作业日志记录
        List<HomeworkLog> allLogs = homeworkLogRepository.findByHomeworkId(homeworkId.intValue());
        
        // 过滤出未提交的成员：
        // 1. 在homework_log表中没有记录的成员
        // 2. 在homework_log表中status为0的成员
        List<String> submittedStudentIds = allLogs.stream()
                .filter(log -> log.getStatus() == 1) // 只保留status为1的记录
                .map(HomeworkLog::getStudentId)
                .toList();
        
        // 过滤出未提交的成员
        List<User> unsubmittedMembers = classMembers.stream()
                .filter(member -> !submittedStudentIds.contains(member.getStudentId()))
                .toList();
        
        // 转换为响应DTO
        List<UnsubmittedMemberResponse> response = unsubmittedMembers.stream()
                .map(this::convertToUnsubmittedMemberResponse)
                .toList();
        
        log.info("获取作业未交成员列表成功: homeworkId={}, totalMembers={}, unsubmittedCount={}, submittedCount={}", 
                homeworkId, classMembers.size(), response.size(), submittedStudentIds.size());
        
        return response;
    }
    
    /**
     * 撤回作业提交
     */
    public void withdrawHomeworkSubmission(Long homeworkId, String userEmail) {
        log.info("撤回作业提交: homeworkId={}, userEmail={}", homeworkId, userEmail);
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 检查作业是否存在
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        // 检查用户是否属于该班级
        if (!homework.getClassCode().equals(user.getClassCode())) {
            throw new BusinessException("PERMISSION-002", "只能撤回本班级的作业");
        }
        
        // 查找作业提交记录
        Optional<HomeworkSubmission> submissionOpt = homeworkSubmissionRepository.findByStudentIdAndHomeworkId(user.getStudentId(), homeworkId);
        if (submissionOpt.isEmpty()) {
            throw new BusinessException("SUBMISSION-004", "未找到作业提交记录");
        }
        
        HomeworkSubmission submission = submissionOpt.get();
        
        // 删除提交的文件
        if (submission.getSubmissionFileUrl() != null && !submission.getSubmissionFileUrl().isEmpty()) {
            try {
                String fileUrl = submission.getSubmissionFileUrl();
                if (fileUrl.startsWith("/uploads/")) {
                    fileUrl = fileUrl.substring(1); // 去掉开头的斜杠
                }
                
                Path filePath = Paths.get(fileUrl);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("删除作业文件: {}", filePath);
                }
            } catch (IOException e) {
                log.error("删除作业文件失败: {}", submission.getSubmissionFileUrl(), e);
                // 不抛出异常，继续执行
            }
        }
        
        // 删除提交记录
        homeworkSubmissionRepository.delete(submission);
        
        // 更新作业日志状态为未提交
        Optional<HomeworkLog> logOpt = homeworkLogRepository.findByHomeworkIdAndStudentId(homeworkId.intValue(), user.getStudentId());
        if (logOpt.isPresent()) {
            HomeworkLog log = logOpt.get();
            log.setStatus(0); // 设置为未提交
            log.setUpdatedAt(LocalDateTime.now());
            homeworkLogRepository.save(log);
        }
        
        log.info("撤回作业提交成功: homeworkId={}, studentId={}", homeworkId, user.getStudentId());
    }
    
    /**
     * 转换为未交成员响应DTO
     */
    private UnsubmittedMemberResponse convertToUnsubmittedMemberResponse(User user) {
        return UnsubmittedMemberResponse.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .realName(user.getRealName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .roleType(user.getRoleType())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    /**
     * 获取班级提交记录（班级空间展示，激励提交）
     * 返回用户名、提交的作业名称、提交时间、提交状态标记、是否首位提交标记
     * 所有用户都能查看本班级的所有提交记录
     */
    public Page<SubmissionRecordResponse> getAllSubmissionRecords(String userEmail, int page, int pageSize) {
        log.info("获取班级提交记录: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        // 获取用户信息
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 确保页码至少为1，然后转换为0基索引
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        
        Page<HomeworkSubmission> submissionPage;
        
        // 所有用户都能查看本班级的提交记录（用于班级空间展示，激励提交）
        if (currentUser.getClassCode() != null && !currentUser.getClassCode().isEmpty()) {
            // 查看本班级的所有提交记录
            submissionPage = homeworkSubmissionRepository.findAllSubmissionsByClassCode(currentUser.getClassCode(), pageable);
        } else {
            // 如果用户没有班级代码，则查看所有记录（主要针对管理员）
            submissionPage = homeworkSubmissionRepository.findAllSubmissions(pageable);
        }
        
        // 转换为响应DTO
        return submissionPage.map(submission -> {
            // 获取用户信息
            User student = userRepository.findByStudentId(submission.getStudentId()).orElse(null);
            
            // 获取作业信息
            Homework homework = homeworkRepository.findById(submission.getHomeworkId()).orElse(null);
            
            // 判断是否是首位提交用户
            LocalDateTime earliestSubmissionTime = homeworkSubmissionRepository.findEarliestSubmissionTimeByHomeworkId(submission.getHomeworkId());
            boolean isFirstSubmission = earliestSubmissionTime != null && 
                    submission.getSubmissionTime().equals(earliestSubmissionTime);
            
            // 判断是否是补交（submission_status为1）
            boolean isLateSubmission = submission.getSubmissionStatus() == 1;
            
            return SubmissionRecordResponse.builder()
                    .id(submission.getId())
                    .studentId(submission.getStudentId())
                    .userName(student != null ? student.getRealName() : "未知用户")
                    .homeworkId(submission.getHomeworkId())
                    .homeworkTitle(homework != null ? homework.getTitle() : "未知作业")
                    .submissionTime(submission.getSubmissionTime())
                    .submissionStatus(submission.getSubmissionStatus())
                    .isLateSubmission(isLateSubmission)
                    .isFirstSubmission(isFirstSubmission)
                    .classCode(submission.getClassCode())
                    .courseName(homework != null ? homework.getCourseName() : null)
                    .build();
        });
    }
} 