package cn.shalee.workupload.service;

import cn.shalee.workupload.dto.request.CreateForumPostRequest;
import cn.shalee.workupload.dto.response.ForumPostResponse;
import cn.shalee.workupload.entity.ForumLike;
import cn.shalee.workupload.entity.ForumPost;
import cn.shalee.workupload.entity.User;
import cn.shalee.workupload.exception.BusinessException;
import cn.shalee.workupload.repository.ForumLikeRepository;
import cn.shalee.workupload.repository.ForumPostRepository;
import cn.shalee.workupload.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 论坛业务逻辑服务
 * @author 31930
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumService {
    
    private final ForumPostRepository forumPostRepository;
    private final ForumLikeRepository forumLikeRepository;
    private final UserRepository userRepository;
    
    /**
     * 发布帖子（主帖或回复）
     */
    @Transactional
    public ForumPostResponse createPost(CreateForumPostRequest request, String userEmail) {
        log.info("发布帖子: userEmail={}, parentId={}", userEmail, request.getParentId());
        
        // 获取用户信息
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 如果是主帖，标题必填
        if (request.getParentId() == null && (request.getTitle() == null || request.getTitle().trim().isEmpty())) {
            throw new BusinessException("FORUM-001", "主帖标题不能为空");
        }
        
        // 如果是回复，检查父帖子是否存在
        if (request.getParentId() != null) {
            ForumPost parentPost = forumPostRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("FORUM-002", "父帖子不存在"));
            
            // 更新父帖子的回复数
            parentPost.setReplyCount(parentPost.getReplyCount() + 1);
            forumPostRepository.save(parentPost);
        }
        
        // 创建帖子
        ForumPost post = ForumPost.builder()
                .studentId(user.getStudentId())
                .classCode(user.getClassCode())
                .parentId(request.getParentId())
                .title(request.getTitle())
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .build();
        
        ForumPost savedPost = forumPostRepository.save(post);
        log.info("帖子发布成功: postId={}, isReply={}", savedPost.getId(), request.getParentId() != null);
        
        return convertToResponse(savedPost, user, false);
    }
    
    /**
     * 获取班级帖子列表（主帖）
     */
    public Page<ForumPostResponse> getClassPosts(String userEmail, int page, int pageSize, String sortType) {
        log.info("获取班级帖子列表: userEmail={}, page={}, pageSize={}, sortType={}", userEmail, page, pageSize, sortType);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        
        Page<ForumPost> postPage;
        if ("hot".equals(sortType)) {
            // 热门排序
            postPage = forumPostRepository.findHotPostsByClassCode(user.getClassCode(), pageable);
        } else {
            // 默认排序（置顶+时间）
            postPage = forumPostRepository.findMainPostsByClassCode(user.getClassCode(), pageable);
        }
        
        return postPage.map(post -> {
            User author = userRepository.findByStudentId(post.getStudentId()).orElse(null);
            boolean isLiked = forumLikeRepository.existsByPostIdAndStudentId(post.getId(), user.getStudentId());
            return convertToResponse(post, author, isLiked);
        });
    }
    
    /**
     * 获取帖子详情
     */
    @Transactional
    public ForumPostResponse getPostDetail(Long postId, String userEmail) {
        log.info("获取帖子详情: postId={}, userEmail={}", postId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("FORUM-003", "帖子不存在"));
        
        // 增加浏览数（只对主帖增加）
        if (post.getParentId() == null) {
            post.setViewCount(post.getViewCount() + 1);
            forumPostRepository.save(post);
        }
        
        User author = userRepository.findByStudentId(post.getStudentId()).orElse(null);
        boolean isLiked = forumLikeRepository.existsByPostIdAndStudentId(post.getId(), user.getStudentId());
        
        return convertToResponse(post, author, isLiked);
    }
    
    /**
     * 获取帖子的所有回复
     */
    public List<ForumPostResponse> getPostReplies(Long postId, String userEmail) {
        log.info("获取帖子回复: postId={}, userEmail={}", postId, userEmail);
        
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        List<ForumPost> replies = forumPostRepository.findByParentIdAndStatusOrderByCreatedAtAsc(postId, 1);
        
        return replies.stream().map(reply -> {
            User author = userRepository.findByStudentId(reply.getStudentId()).orElse(null);
            boolean isLiked = forumLikeRepository.existsByPostIdAndStudentId(reply.getId(), currentUser.getStudentId());
            return convertToResponse(reply, author, isLiked);
        }).collect(Collectors.toList());
    }
    
    /**
     * 点赞/取消点赞
     */
    @Transactional
    public boolean toggleLike(Long postId, String userEmail) {
        log.info("切换点赞状态: postId={}, userEmail={}", postId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("FORUM-003", "帖子不存在"));
        
        boolean isLiked = forumLikeRepository.existsByPostIdAndStudentId(postId, user.getStudentId());
        
        if (isLiked) {
            // 取消点赞
            forumLikeRepository.deleteByPostIdAndStudentId(postId, user.getStudentId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            forumPostRepository.save(post);
            log.info("取消点赞成功: postId={}, studentId={}", postId, user.getStudentId());
            return false;
        } else {
            // 点赞
            ForumLike like = ForumLike.builder()
                    .postId(postId)
                    .studentId(user.getStudentId())
                    .build();
            forumLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            forumPostRepository.save(post);
            log.info("点赞成功: postId={}, studentId={}", postId, user.getStudentId());
            return true;
        }
    }
    
    /**
     * 删除帖子
     */
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        log.info("删除帖子: postId={}, userEmail={}", postId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("FORUM-003", "帖子不存在"));
        
        // 权限检查：只能删除自己的帖子，或管理员/学委可以删除任意帖子
        if (!post.getStudentId().equals(user.getStudentId()) && 
            user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("FORUM-004", "无权删除此帖子");
        }
        
        // 软删除
        post.setStatus(0);
        forumPostRepository.save(post);
        
        // 如果是回复，更新父帖子的回复数
        if (post.getParentId() != null) {
            forumPostRepository.findById(post.getParentId()).ifPresent(parentPost -> {
                parentPost.setReplyCount(Math.max(0, parentPost.getReplyCount() - 1));
                forumPostRepository.save(parentPost);
            });
        }
        
        log.info("帖子删除成功: postId={}", postId);
    }
    
    /**
     * 置顶/取消置顶帖子（仅管理员/学委）
     */
    @Transactional
    public void toggleTop(Long postId, String userEmail) {
        log.info("切换置顶状态: postId={}, userEmail={}", postId, userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        // 权限检查：只有管理员和学委可以置顶
        if (user.getRoleType() != 0 && user.getRoleType() != 2) {
            throw new BusinessException("FORUM-005", "无权置顶帖子");
        }
        
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("FORUM-003", "帖子不存在"));
        
        // 只能置顶主帖
        if (post.getParentId() != null) {
            throw new BusinessException("FORUM-006", "只能置顶主帖");
        }
        
        post.setIsTop(!post.getIsTop());
        forumPostRepository.save(post);
        
        log.info("置顶状态切换成功: postId={}, isTop={}", postId, post.getIsTop());
    }
    
    /**
     * 获取我的帖子
     */
    public Page<ForumPostResponse> getMyPosts(String userEmail, int page, int pageSize) {
        log.info("获取我的帖子: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("USER-001", "用户不存在"));
        
        int pageIndex = Math.max(1, page) - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        
        Page<ForumPost> postPage = forumPostRepository.findByStudentIdAndStatusOrderByCreatedAtDesc(
                user.getStudentId(), 1, pageable);
        
        return postPage.map(post -> {
            boolean isLiked = forumLikeRepository.existsByPostIdAndStudentId(post.getId(), user.getStudentId());
            return convertToResponse(post, user, isLiked);
        });
    }
    
    /**
     * 转换为响应DTO
     */
    private ForumPostResponse convertToResponse(ForumPost post, User author, boolean isLiked) {
        return ForumPostResponse.builder()
                .id(post.getId())
                .studentId(post.getStudentId())
                .studentName(author != null ? author.getRealName() : "未知用户")
                .studentAvatar(author != null ? author.getAvatarUrl() : null)
                .nickname(author != null ? author.getNickname() : null)
                .classCode(post.getClassCode())
                .parentId(post.getParentId())
                .title(post.getTitle())
                .content(post.getContent())
                .attachmentUrl(post.getAttachmentUrl())
                .attachmentName(post.getAttachmentName())
                .likeCount(post.getLikeCount())
                .replyCount(post.getReplyCount())
                .viewCount(post.getViewCount())
                .isTop(post.getIsTop())
                .isHot(post.getIsHot())
                .isLiked(isLiked)
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}


