package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.HomeworkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 作业提交日志数据访问层
 * @author 31930
 */
@Repository
public interface HomeworkLogRepository extends JpaRepository<HomeworkLog, Integer> {
    
    /**
     * 根据作业ID和学号查找日志记录
     */
    Optional<HomeworkLog> findByHomeworkIdAndStudentId(Integer homeworkId, String studentId);
    
    /**
     * 根据作业ID查找所有日志记录
     */
    List<HomeworkLog> findByHomeworkId(Integer homeworkId);
    
    /**
     * 根据学号查找所有日志记录
     */
    List<HomeworkLog> findByStudentId(String studentId);
    
    /**
     * 根据作业ID和状态查找日志记录
     */
    List<HomeworkLog> findByHomeworkIdAndStatus(Integer homeworkId, Integer status);
    
    /**
     * 根据学号和状态查找日志记录
     */
    List<HomeworkLog> findByStudentIdAndStatus(String studentId, Integer status);
    
    /**
     * 检查学生是否已提交作业
     */
    boolean existsByHomeworkIdAndStudentId(Integer homeworkId, String studentId);
    
    /**
     * 根据作业ID统计已提交数量
     */
    long countByHomeworkIdAndStatus(Integer homeworkId, Integer status);
    
    /**
     * 根据班级代码查找作业提交状态
     */
    @Query("SELECT hl FROM HomeworkLog hl JOIN Homework h ON hl.homeworkId = h.id WHERE h.classCode = :classCode AND hl.studentId = :studentId")
    List<HomeworkLog> findByClassCodeAndStudentId(@Param("classCode") String classCode, @Param("studentId") String studentId);
} 