package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.HomeworkSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    
    /**
     * 查询指定作业的最早提交时间
     */
    @Query("SELECT MIN(hs.submissionTime) FROM HomeworkSubmission hs WHERE hs.homeworkId = :homeworkId")
    LocalDateTime findEarliestSubmissionTimeByHomeworkId(@Param("homeworkId") Long homeworkId);
    
    /**
     * 查询所有提交记录（用于生成提交记录列表）
     */
    @Query("SELECT hs FROM HomeworkSubmission hs ORDER BY hs.submissionTime DESC")
    Page<HomeworkSubmission> findAllSubmissions(Pageable pageable);
    
    /**
     * 按班级代码查询所有提交记录
     */
    @Query("SELECT hs FROM HomeworkSubmission hs WHERE hs.classCode = :classCode ORDER BY hs.submissionTime DESC")
    Page<HomeworkSubmission> findAllSubmissionsByClassCode(@Param("classCode") String classCode, Pageable pageable);
} 