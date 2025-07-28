package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.CreateHomeworkRequest;
import cn.shalee.workupload.dto.response.HomeworkResponse;
import cn.shalee.workupload.entity.Homework;
import cn.shalee.workupload.repository.HomeworkRepository;
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
    
    public HomeworkResponse createHomework(CreateHomeworkRequest request) {
        log.info("创建作业: title={}, classCode={}", request.getTitle(), request.getClassCode());
        
        Homework homework = Homework.builder()
                .classCode(request.getClassCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .attachmentUrl(request.getAttachmentUrl())
                .publishTime(request.getPublishTime())
                .deadline(request.getDeadline())
                .totalScore(request.getTotalScore())
                .status(request.getStatus())
                .build();
        
        Homework savedHomework = homeworkRepository.save(homework);
        log.info("作业创建成功: id={}", savedHomework.getId());
        
        return convertToResponse(savedHomework);
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
    
    public HomeworkResponse updateHomework(Long id, CreateHomeworkRequest request) {
        log.info("更新作业: id={}", id);
        
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOMEWORK-001", "作业不存在"));
        
        homework.setClassCode(request.getClassCode());
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setAttachmentUrl(request.getAttachmentUrl());
        homework.setPublishTime(request.getPublishTime());
        homework.setDeadline(request.getDeadline());
        homework.setTotalScore(request.getTotalScore());
        homework.setStatus(request.getStatus());
        
        Homework updatedHomework = homeworkRepository.save(homework);
        log.info("作业更新成功: id={}", updatedHomework.getId());
        
        return convertToResponse(updatedHomework);
    }
    
    public void deleteHomework(Long id) {
        log.info("删除作业: id={}", id);
        
        if (!homeworkRepository.existsById(id)) {
            throw new BusinessException("HOMEWORK-001", "作业不存在");
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
                .publishTime(homework.getPublishTime())
                .deadline(homework.getDeadline())
                .totalScore(homework.getTotalScore())
                .status(homework.getStatus())
                .createdAt(homework.getCreatedAt())
                .updatedAt(homework.getUpdatedAt())
                .build();
    }
} 