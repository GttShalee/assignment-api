package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    
    Class findByClassCode(String classCode);
} 