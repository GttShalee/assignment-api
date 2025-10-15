package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 论坛点赞记录数据访问层
 * @author 31930
 */
@Repository
public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    
    /**
     * 查询用户是否已点赞
     */
    Optional<ForumLike> findByPostIdAndStudentId(Long postId, String studentId);
    
    /**
     * 检查用户是否已点赞
     */
    boolean existsByPostIdAndStudentId(Long postId, String studentId);
    
    /**
     * 删除点赞记录
     */
    void deleteByPostIdAndStudentId(Long postId, String studentId);
}


