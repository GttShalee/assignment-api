package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.HomeworkSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 作业提交数据访问层
 * @author 31930
 */
@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    
    /**
     * 根据学号和作业ID查找提交记录
     */
    Optional<HomeworkSubmission> findByStudentIdAndHomeworkId(String studentId, Long homeworkId);
    
    /**
     * 根据作业ID查找所有提交记录
     */
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
    
    /**
     * 根据作业ID分页查找提交记录
     */
    Page<HomeworkSubmission> findByHomeworkId(Long homeworkId, Pageable pageable);
    
    /**
     * 根据班级代码查找所有提交记录
     */
    List<HomeworkSubmission> findByClassCode(String classCode);
    
    /**
     * 根据班级代码分页查找提交记录
     */
    Page<HomeworkSubmission> findByClassCode(String classCode, Pageable pageable);
    
    /**
     * 根据学号查找所有提交记录
     */
    List<HomeworkSubmission> findByStudentId(String studentId);
    
    /**
     * 根据学号分页查找提交记录
     */
    Page<HomeworkSubmission> findByStudentId(String studentId, Pageable pageable);
    
    /**
     * 根据作业ID和班级代码查找提交记录
     */
    List<HomeworkSubmission> findByHomeworkIdAndClassCode(Long homeworkId, String classCode);
    
    /**
     * 根据作业ID和班级代码分页查找提交记录
     */
    Page<HomeworkSubmission> findByHomeworkIdAndClassCode(Long homeworkId, String classCode, Pageable pageable);
    
    /**
     * 根据作业ID统计提交数量
     */
    long countByHomeworkId(Long homeworkId);
    
    /**
     * 根据作业ID和班级代码统计提交数量
     */
    long countByHomeworkIdAndClassCode(Long homeworkId, String classCode);
    
    /**
     * 根据作业ID和提交状态查找提交记录
     */
    List<HomeworkSubmission> findByHomeworkIdAndSubmissionStatus(Long homeworkId, Integer submissionStatus);
    
    /**
     * 检查学生是否已提交作业
     */
    boolean existsByStudentIdAndHomeworkId(String studentId, Long homeworkId);
} 