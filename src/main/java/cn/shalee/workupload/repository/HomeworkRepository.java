package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    
    Page<Homework> findByClassCode(String classCode, Pageable pageable);
    
    Page<Homework> findByStatus(Integer status, Pageable pageable);
    
    Page<Homework> findByClassCodeAndStatus(String classCode, Integer status, Pageable pageable);
    
    /**
     * 查询指定时间范围内截止的作业
     */
    List<Homework> findByDeadlineBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询即将截止的作业（用于定时提醒）
     */
    List<Homework> findByDeadlineAfterAndDeadlineBefore(LocalDateTime after, LocalDateTime before);
} 