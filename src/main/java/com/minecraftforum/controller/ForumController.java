package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.dto.CommentDTO;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.dto.ReplyDTO;
import com.minecraftforum.entity.Comment;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.ForumReply;
import com.minecraftforum.mapper.CommentMapper;
import com.minecraftforum.mapper.ForumReplyMapper;
import com.minecraftforum.service.ForumService;
import com.minecraftforum.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛控制器
 * 处理帖子、评论、回复的CRUD操作以及点赞等互动功能
 */
@Tag(name = "论坛管理", description = "帖子、评论、回复相关的增删改查、点赞等接口")
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ForumController {

    private final ForumService forumService;
    private final CommentMapper commentMapper;
    private final ForumReplyMapper replyMapper;
    private final SecurityUtil securityUtil;

    /**
     * 获取帖子列表
     */
    @Operation(summary = "获取帖子列表", description = "分页获取帖子列表，支持按分类、关键词、作者筛选和排序")
    @GetMapping("/posts")
    @AnonymousAccess
    public Result<Map<String, Object>> getPostList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "分类代码")
            @RequestParam(required = false) String category,
            @Parameter(description = "搜索关键词（标题、内容）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "作者关键词（用户名、昵称、邮箱）")
            @RequestParam(required = false) String authorKeyword,
            @Parameter(description = "排序方式：createTime-发布时间，viewCount-浏览量，likeCount-点赞量")
            @RequestParam(required = false) String sortBy) {

        Page<ForumPost> pageObj = new Page<>(page, pageSize);
        IPage<ForumPostDTO> result = forumService.getPostList(pageObj, category, keyword, authorKeyword, sortBy);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取帖子详情
     */
    @Operation(summary = "获取帖子详情", description = "根据ID获取帖子的详细信息")
    @GetMapping("/posts/{id}")
    @AnonymousAccess
    public Result<ForumPostDTO> getPostById(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id) {
        ForumPostDTO post = forumService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 创建帖子
     */
    @Operation(summary = "创建帖子", description = "发布新帖子，需要post:create权限")
    @PostMapping("/posts")
    public Result<ForumPost> createPost(
            @Parameter(description = "帖子信息", required = true)
            @RequestBody ForumPost post) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        post.setAuthorId(userId);
        ForumPost created = forumService.createPost(post);
        return Result.success(created);
    }

    /**
     * 更新帖子
     */
    @Operation(summary = "更新帖子", description = "更新帖子信息，只能更新自己发布的帖子")
    @PutMapping("/posts/{id}")
    public Result<ForumPost> updatePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "帖子信息", required = true)
            @RequestBody ForumPost post) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        ForumPostDTO existing = forumService.getPostById(id);
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

    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "删除帖子，只能删除自己发布的帖子")
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        ForumPostDTO post = forumService.getPostById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此帖子");
        }

        forumService.deletePost(id);
        return Result.success(null);
    }

    /**
     * 点赞帖子
     */
    @Operation(summary = "点赞帖子", description = "为帖子点赞")
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.likePost(id, userId);
        return Result.success(null);
    }

    /**
     * 取消点赞帖子
     */
    @Operation(summary = "取消点赞", description = "取消对帖子的点赞")
    @DeleteMapping("/posts/{id}/like")
    public Result<Void> unlikePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.unlikePost(id, userId);
        return Result.success(null);
    }

    /**
     * 创建评论
     */
    @Operation(summary = "创建评论", description = "为帖子创建评论，需要comment:create权限")
    @PostMapping("/posts/{postId}/comments")
    public Result<Comment> createComment(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "评论内容", required = true)
            @RequestBody Map<String, String> body) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        String content = body.get("content");
        Comment comment = forumService.createComment(postId, userId, content);
        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除评论，只有评论作者可以删除（级联删除所有子回复）")
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查权限：只有评论作者可以删除评论
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        
        // 检查是否是评论作者
        if (!comment.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此评论");
        }
        
        forumService.deleteComment(id);
        return Result.success(null);
    }

    /**
     * 创建回复
     */
    @Operation(summary = "创建回复", description = "为评论创建回复，可以@其他用户，支持回复回复（嵌套回复）")
    @PostMapping("/comments/{commentId}/replies")
    public Result<ForumReply> createReply(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "回复内容", required = true)
            @RequestBody Map<String, Object> body) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        String content = (String) body.get("content");
        Long targetUserId = body.get("targetUserId") != null ?
                Long.valueOf(body.get("targetUserId").toString()) : null;
        Long parentId = body.get("parentId") != null ?
                Long.valueOf(body.get("parentId").toString()) : null;

        ForumReply reply = forumService.createReply(commentId, userId, content, targetUserId, parentId);
        return Result.success(reply);
    }

    /**
     * 删除回复
     */
    @Operation(summary = "删除回复", description = "删除回复，只有回复作者可以删除（级联删除所有子回复）")
    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteReply(
            @Parameter(description = "回复ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查权限：只有回复作者可以删除回复
        ForumReply reply = replyMapper.selectById(id);
        if (reply == null) {
            return Result.error(404, "回复不存在");
        }
        
        // 检查是否是回复作者
        if (!reply.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此回复");
        }
        
        forumService.deleteReply(id);
        return Result.success(null);
    }


    /**
     * 点赞评论
     */
    @Operation(summary = "点赞评论", description = "为评论点赞")
    @PostMapping("/comments/{id}/like")
    public Result<Void> likeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.likeComment(id, userId);
        return Result.success(null);
    }

    /**
     * 点赞回复
     */
    @Operation(summary = "点赞回复", description = "为回复点赞")
    @PostMapping("/replies/{id}/like")
    public Result<Void> likeReply(
            @Parameter(description = "回复ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.likeReply(id, userId);
        return Result.success(null);
    }
    
    /**
     * 取消点赞评论
     */
    @Operation(summary = "取消点赞评论", description = "取消对评论的点赞")
    @DeleteMapping("/comments/{id}/like")
    public Result<Void> unlikeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.unlikeComment(id, userId);
        return Result.success(null);
    }
    
    /**
     * 取消点赞回复
     */
    @Operation(summary = "取消点赞回复", description = "取消对回复的点赞")
    @DeleteMapping("/replies/{id}/like")
    public Result<Void> unlikeReply(
            @Parameter(description = "回复ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        forumService.unlikeReply(id, userId);
        return Result.success(null);
    }
    
    /**
     * 获取帖子评论列表（分页）
     */
    @Operation(summary = "获取帖子评论列表", description = "分页获取帖子的评论列表，返回树形结构")
    @GetMapping("/posts/{postId}/comments")
    @AnonymousAccess
    public Result<Map<String, Object>> getCommentsByPostId(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<CommentDTO> result = forumService.getCommentsByPostId(postId, page, pageSize);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取用户评论列表
     */
    @Operation(summary = "获取用户评论列表", description = "获取指定用户的所有评论")
    @GetMapping("/users/{userId}/comments")
    @AnonymousAccess
    public Result<List<CommentDTO>> getUserComments(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        
        List<CommentDTO> comments = forumService.getUserComments(userId);
        return Result.success(comments);
    }
    
    /**
     * 获取评论的子回复列表
     */
    @Operation(summary = "获取评论的子回复列表", description = "获取指定评论的所有子回复")
    @GetMapping("/comments/{commentId}/replies")
    @AnonymousAccess
    public Result<List<ReplyDTO>> getRepliesByCommentId(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        
        List<ReplyDTO> replies = forumService.getRepliesByCommentId(commentId);
        return Result.success(replies);
    }
}
