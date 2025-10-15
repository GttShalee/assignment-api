package cn.shalee.workupload.controller;

import cn.shalee.workupload.dto.request.CreateForumPostRequest;
import cn.shalee.workupload.dto.response.ForumPostResponse;
import cn.shalee.workupload.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 论坛控制器
 * @author 31930
 */
@Slf4j
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {
    
    private final ForumService forumService;
    
    /**
     * 发布帖子（主帖或回复）
     */
    @PostMapping("/post")
    public ResponseEntity<ForumPostResponse> createPost(@Valid @RequestBody CreateForumPostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("收到发帖请求: userEmail={}, parentId={}", userEmail, request.getParentId());
        
        ForumPostResponse response = forumService.createPost(request, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取班级帖子列表
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<ForumPostResponse>> getClassPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "default") String sortType) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("获取班级帖子列表: userEmail={}, page={}, pageSize={}, sortType={}", userEmail, page, pageSize, sortType);
        
        Page<ForumPostResponse> response = forumService.getClassPosts(userEmail, page, pageSize, sortType);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<ForumPostResponse> getPostDetail(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("获取帖子详情: postId={}, userEmail={}", postId, userEmail);
        
        ForumPostResponse response = forumService.getPostDetail(postId, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取帖子的所有回复
     */
    @GetMapping("/post/{postId}/replies")
    public ResponseEntity<List<ForumPostResponse>> getPostReplies(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("获取帖子回复: postId={}, userEmail={}", postId, userEmail);
        
        List<ForumPostResponse> response = forumService.getPostReplies(postId, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 点赞/取消点赞
     */
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("切换点赞状态: postId={}, userEmail={}", postId, userEmail);
        
        boolean isLiked = forumService.toggleLike(postId, userEmail);
        
        return ResponseEntity.ok(Map.of(
            "message", isLiked ? "点赞成功" : "取消点赞成功",
            "is_liked", isLiked
        ));
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("删除帖子: postId={}, userEmail={}", postId, userEmail);
        
        forumService.deletePost(postId, userEmail);
        
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
    
    /**
     * 置顶/取消置顶
     */
    @PostMapping("/post/{postId}/top")
    public ResponseEntity<Map<String, String>> toggleTop(@PathVariable Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("切换置顶状态: postId={}, userEmail={}", postId, userEmail);
        
        forumService.toggleTop(postId, userEmail);
        
        return ResponseEntity.ok(Map.of("message", "操作成功"));
    }
    
    /**
     * 获取我的帖子
     */
    @GetMapping("/my-posts")
    public ResponseEntity<Page<ForumPostResponse>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        log.info("获取我的帖子: userEmail={}, page={}, pageSize={}", userEmail, page, pageSize);
        
        Page<ForumPostResponse> response = forumService.getMyPosts(userEmail, page, pageSize);
        return ResponseEntity.ok(response);
    }
}


