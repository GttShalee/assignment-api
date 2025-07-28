package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    
    Page<Homework> findByClassCode(String classCode, Pageable pageable);
    
    Page<Homework> findByStatus(Integer status, Pageable pageable);
    
    Page<Homework> findByClassCodeAndStatus(String classCode, Integer status, Pageable pageable);
} 