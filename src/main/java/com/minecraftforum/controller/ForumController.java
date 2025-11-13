package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.common.Result;
import com.minecraftforum.config.AnonymousAccess;
import com.minecraftforum.entity.Comment;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.ForumReply;
import com.minecraftforum.mapper.CommentMapper;
import com.minecraftforum.mapper.ForumReplyMapper;
import com.minecraftforum.service.ForumService;
import com.minecraftforum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final JwtUtil jwtUtil;
    private final CommentMapper commentMapper;
    private final ForumReplyMapper replyMapper;

    @GetMapping("/posts")
    @AnonymousAccess
    public Result<Map<String, Object>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<ForumPost> pageObj = new Page<>(page, pageSize);
        IPage<ForumPost> result = forumService.getPostList(pageObj, category, keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());

        return Result.success(data);
    }

    @GetMapping("/posts/{id}")
    @AnonymousAccess
    public Result<ForumPost> getPostById(@PathVariable Long id) {
        ForumPost post = forumService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody ForumPost post, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        post.setAuthorId(userId);
        ForumPost created = forumService.createPost(post);
        return Result.success(created);
    }

    @PutMapping("/posts/{id}")
    public Result<ForumPost> updatePost(@PathVariable Long id, @RequestBody ForumPost post,
                                        HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        ForumPost existing = forumService.getPostById(id);
        if (existing == null) {
            return Result.error(404, "帖子不存在");
        }
        if (!existing.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限修改此帖子");
        }

        post.setId(id);
        ForumPost updated = forumService.updatePost(post);
        return Result.success(updated);
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        ForumPost post = forumService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此帖子");
        }

        forumService.deletePost(id);
        return Result.success(null);
    }

    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        forumService.likePost(id, userId);
        return Result.success(null);
    }

    @DeleteMapping("/posts/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        forumService.unlikePost(id, userId);
        return Result.success(null);
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<Comment> createComment(@PathVariable Long postId, @RequestBody Map<String, String> body,
                                         HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String content = body.get("content");

        Comment comment = forumService.createComment(postId, userId, content);
        return Result.success(comment);
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 检查权限：只有评论作者或帖子作者可以删除评论
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        
        // 检查是否是评论作者
        boolean isCommentAuthor = comment.getAuthorId().equals(userId);
        
        // 检查是否是帖子作者
        ForumPost post = forumService.getPostById(comment.getResourceId());
        boolean isPostAuthor = post != null && post.getAuthorId().equals(userId);
        
        if (!isCommentAuthor && !isPostAuthor) {
            return Result.error(403, "没有权限删除此评论");
        }
        
        forumService.deleteComment(id);
        return Result.success(null);
    }

    @PostMapping("/comments/{commentId}/replies")
    public Result<ForumReply> createReply(@PathVariable Long commentId, @RequestBody Map<String, Object> body,
                                          HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String content = (String) body.get("content");
        Long targetUserId = body.get("targetUserId") != null ?
                Long.valueOf(body.get("targetUserId").toString()) : null;

        ForumReply reply = forumService.createReply(commentId, userId, content, targetUserId);
        return Result.success(reply);
    }

    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteReply(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 检查权限：只有回复作者、评论作者或帖子作者可以删除回复
        ForumReply reply = replyMapper.selectById(id);
        if (reply == null) {
            return Result.error(404, "回复不存在");
        }
        
        // 检查是否是回复作者
        boolean isReplyAuthor = reply.getAuthorId().equals(userId);
        
        // 检查是否是评论作者
        Comment comment = commentMapper.selectById(reply.getCommentId());
        boolean isCommentAuthor = comment != null && comment.getAuthorId().equals(userId);
        
        // 检查是否是帖子作者
        boolean isPostAuthor = false;
        if (comment != null) {
            ForumPost post = forumService.getPostById(comment.getResourceId());
            isPostAuthor = post != null && post.getAuthorId().equals(userId);
        }
        
        if (!isReplyAuthor && !isCommentAuthor && !isPostAuthor) {
            return Result.error(403, "没有权限删除此回复");
        }
        
        forumService.deleteReply(id);
        return Result.success(null);
    }

    @PostMapping("/comments/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        forumService.likeComment(id, userId);
        return Result.success(null);
    }

    @PostMapping("/replies/{id}/like")
    public Result<Void> likeReply(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        forumService.likeReply(id, userId);
        return Result.success(null);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

