package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param email 用户邮箱
     * 用来判断邮箱是否已经存在
     * @return true or false
     */
    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    Optional<User> findByEmail(String email);
    Optional<User> findByStudentId(String studentId);

    /**
     * 根据班级代码和角色类型查找用户
     */
    List<User> findByClassCodeAndRoleType(String classCode, Integer roleType);
}