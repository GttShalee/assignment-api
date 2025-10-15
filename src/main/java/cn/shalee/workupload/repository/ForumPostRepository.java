package cn.shalee.workupload.repository;

import cn.shalee.workupload.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 论坛帖子数据访问层
 * @author 31930
 */
@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    
    /**
     * 查询班级主帖列表（置顶优先，按时间倒序）
     */
    @Query("SELECT fp FROM ForumPost fp WHERE fp.parentId IS NULL AND fp.classCode = :classCode AND fp.status = 1 ORDER BY fp.isTop DESC, fp.createdAt DESC")
    Page<ForumPost> findMainPostsByClassCode(@Param("classCode") String classCode, Pageable pageable);
    
    /**
     * 查询某个帖子的所有回复
     */
    List<ForumPost> findByParentIdAndStatusOrderByCreatedAtAsc(Long parentId, Integer status);
    
    /**
     * 查询用户发布的所有帖子
     */
    Page<ForumPost> findByStudentIdAndStatusOrderByCreatedAtDesc(String studentId, Integer status, Pageable pageable);
    
    /**
     * 查询热门帖子（按综合热度排序）
     */
    @Query("SELECT fp FROM ForumPost fp WHERE fp.parentId IS NULL AND fp.classCode = :classCode AND fp.status = 1 ORDER BY (fp.likeCount + fp.replyCount * 2 + fp.viewCount * 0.1) DESC, fp.createdAt DESC")
    Page<ForumPost> findHotPostsByClassCode(@Param("classCode") String classCode, Pageable pageable);
    
    /**
     * 统计用户发帖数
     */
    long countByStudentIdAndParentIdIsNullAndStatus(String studentId, Integer status);
    
    /**
     * 统计用户回复数
     */
    long countByStudentIdAndParentIdIsNotNullAndStatus(String studentId, Integer status);
}

