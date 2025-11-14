package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.CommentDTO;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.dto.ReplyDTO;
import com.minecraftforum.entity.*;
import com.minecraftforum.mapper.*;
import com.minecraftforum.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {
    
    private final ForumPostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ForumReplyMapper replyMapper;
    private final LikeMapper likeMapper;
    private final UserMapper userMapper;
    private final com.minecraftforum.util.SecurityUtil securityUtil;
    
    @Override
    public IPage<ForumPostDTO> getPostList(Page<ForumPost> page, String category, String keyword, String authorKeyword, String sortBy) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(ForumPost::getCategory, category);
        }
        
        // 关键词搜索（标题、内容）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ForumPost::getTitle, keyword)
                    .or().like(ForumPost::getContent, keyword));
        }
        
        // 作者搜索（通过authorId查询）
        if (StringUtils.hasText(authorKeyword)) {
            // 先根据关键词查找用户
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.and(w -> w.like(User::getUsername, authorKeyword)
                    .or().like(User::getNickname, authorKeyword)
                    .or().like(User::getEmail, authorKeyword));
            List<User> users = userMapper.selectList(userWrapper);
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(ForumPost::getAuthorId, userIds);
            } else {
                // 如果没有找到匹配的用户，返回空结果
                wrapper.eq(ForumPost::getAuthorId, -1L);
            }
        }
        
        wrapper.eq(ForumPost::getStatus, "NORMAL");
        
        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "viewCount":
                    wrapper.orderByDesc(ForumPost::getViewCount);
                    break;
                case "likeCount":
                    wrapper.orderByDesc(ForumPost::getLikeCount);
                    break;
                case "createTime":
                default:
                    wrapper.orderByDesc(ForumPost::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(ForumPost::getCreateTime);
        }
        
        IPage<ForumPost> postPage = postMapper.selectPage(page, wrapper);
        
        // 转换为DTO并填充作者信息
        Long currentUserId = securityUtil.getCurrentUserId();
        IPage<ForumPostDTO> dtoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        List<ForumPostDTO> dtoList = postPage.getRecords().stream()
                .map(post -> convertToDTO(post, currentUserId))
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 将ForumPost实体转换为ForumPostDTO，并填充作者信息
     */
    private ForumPostDTO convertToDTO(ForumPost post) {
        return convertToDTO(post, null);
    }
    
    /**
     * 将ForumPost实体转换为ForumPostDTO，并填充作者信息和点赞状态
     */
    private ForumPostDTO convertToDTO(ForumPost post, Long currentUserId) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());
        dto.setAuthorId(post.getAuthorId());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setStatus(post.getStatus());
        dto.setCreateTime(post.getCreateTime());
        dto.setUpdateTime(post.getUpdateTime());
        
        // 查询作者信息
        User author = userMapper.selectById(post.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatar());
        }
        
        // 如果用户已登录，检查是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Like::getPostId, post.getId());
            likeWrapper.eq(Like::getUserId, currentUserId);
            dto.setIsLiked(likeMapper.selectOne(likeWrapper) != null);
        } else {
            dto.setIsLiked(false);
        }
        
        return dto;
    }
    
    @Override
    public ForumPostDTO getPostById(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            return null;
        }
        if ("NORMAL".equals(post.getStatus())) {
            viewPost(id);
        }
        return convertToDTO(post, securityUtil.getCurrentUserId());
    }
    
    @Override
    public ForumPost createPost(ForumPost post) {
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("NORMAL");
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postMapper.insert(post);
        return post;
    }
    
    @Override
    public ForumPost updatePost(ForumPost post) {
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        return post;
    }
    
    @Override
    @Transactional
    public void deletePost(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post != null) {
            post.setStatus("DELETED");
            postMapper.updateById(post);
            
            // 级联删除该帖子的所有评论和回复
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getResourceId, id);
            List<Comment> comments = commentMapper.selectList(commentWrapper);
            
            for (Comment comment : comments) {
                // 删除该评论的所有回复
                LambdaQueryWrapper<ForumReply> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.eq(ForumReply::getCommentId, comment.getId());
                replyMapper.delete(replyWrapper);
            }
            
            // 删除所有评论
            commentMapper.delete(commentWrapper);
        }
    }
    
    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getPostId, postId);
        wrapper.eq(Like::getUserId, userId);
        
        if (likeMapper.selectOne(wrapper) == null) {
            Like like = new Like();
            like.setUserId(userId);
            like.setPostId(postId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            
            ForumPost post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + 1);
                postMapper.updateById(post);
            }
        }
    }
    
    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getPostId, postId);
        wrapper.eq(Like::getUserId, userId);
        
        Like existingLike = likeMapper.selectOne(wrapper);
        if (existingLike != null) {
            likeMapper.delete(wrapper);
            
            ForumPost post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                postMapper.updateById(post);
            }
        }
    }
    
    @Override
    @Transactional
    public void viewPost(Long postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            postMapper.updateById(post);
        }
    }
    
    @Override
    @Transactional
    public Comment createComment(Long postId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setResourceId(postId); // 复用字段，存储postId
        comment.setAuthorId(userId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        
        ForumPost post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postMapper.updateById(post);
        }
        
        return comment;
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            // 级联删除该评论的所有回复（包括嵌套回复）
            deleteRepliesByCommentId(commentId);
            
            ForumPost post = postMapper.selectById(comment.getResourceId());
            if (post != null) {
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                postMapper.updateById(post);
            }
            commentMapper.deleteById(commentId);
        }
    }
    
    /**
     * 递归删除评论的所有回复（包括嵌套回复）
     */
    private void deleteRepliesByCommentId(Long commentId) {
        LambdaQueryWrapper<ForumReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumReply::getCommentId, commentId);
        List<ForumReply> replies = replyMapper.selectList(wrapper);
        
        for (ForumReply reply : replies) {
            // 递归删除该回复的所有子回复
            deleteRepliesByParentId(reply.getId());
        }
        
        // 删除所有回复（包括直接回复和嵌套回复）
        replyMapper.delete(wrapper);
    }
    
    /**
     * 递归删除父回复的所有子回复
     */
    private void deleteRepliesByParentId(Long parentId) {
        LambdaQueryWrapper<ForumReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumReply::getParentId, parentId);
        List<ForumReply> children = replyMapper.selectList(wrapper);
        
        for (ForumReply child : children) {
            // 递归删除子回复的子回复
            deleteRepliesByParentId(child.getId());
        }
        
        // 删除所有子回复
        replyMapper.delete(wrapper);
    }
    
    @Override
    public ForumReply createReply(Long commentId, Long userId, String content, Long targetUserId, Long parentId) {
        ForumReply reply = new ForumReply();
        reply.setCommentId(commentId);
        reply.setParentId(null); // 所有回复都是根评论的直接子回复，只支持两层结构
        reply.setAuthorId(userId);
        reply.setTargetUserId(targetUserId); // 记录被回复的用户ID，用于显示"回复 @用户名"
        reply.setContent(content);
        reply.setLikeCount(0);
        reply.setCreateTime(LocalDateTime.now());
        replyMapper.insert(reply);
        return reply;
    }
    
    @Override
    @Transactional
    public void deleteReply(Long replyId) {
        ForumReply reply = replyMapper.selectById(replyId);
        if (reply != null) {
            // 级联删除该回复的所有子回复
            deleteRepliesByParentId(replyId);
            replyMapper.deleteById(replyId);
        }
    }
    
    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getCommentId, commentId);
        wrapper.eq(Like::getUserId, userId);
        
        if (likeMapper.selectOne(wrapper) == null) {
            Like like = new Like();
            like.setUserId(userId);
            like.setCommentId(commentId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(comment.getLikeCount() + 1);
                commentMapper.updateById(comment);
            }
        }
    }
    
    @Override
    @Transactional
    public void likeReply(Long replyId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getReplyId, replyId);
        wrapper.eq(Like::getUserId, userId);
        
        if (likeMapper.selectOne(wrapper) == null) {
            Like like = new Like();
            like.setUserId(userId);
            like.setReplyId(replyId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            
            ForumReply reply = replyMapper.selectById(replyId);
            if (reply != null) {
                reply.setLikeCount(reply.getLikeCount() + 1);
                replyMapper.updateById(reply);
            }
        }
    }
    
    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getCommentId, commentId);
        wrapper.eq(Like::getUserId, userId);
        
        Like existingLike = likeMapper.selectOne(wrapper);
        if (existingLike != null) {
            likeMapper.delete(wrapper);
            
            Comment comment = commentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                commentMapper.updateById(comment);
            }
        }
    }
    
    @Override
    @Transactional
    public void unlikeReply(Long replyId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getReplyId, replyId);
        wrapper.eq(Like::getUserId, userId);
        
        Like existingLike = likeMapper.selectOne(wrapper);
        if (existingLike != null) {
            likeMapper.delete(wrapper);
            
            ForumReply reply = replyMapper.selectById(replyId);
            if (reply != null) {
                reply.setLikeCount(Math.max(0, reply.getLikeCount() - 1));
                replyMapper.updateById(reply);
            }
        }
    }
    
    @Override
    public IPage<CommentDTO> getCommentsByPostId(Long postId, Integer page, Integer pageSize) {
        Page<Comment> commentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getResourceId, postId);
        wrapper.orderByDesc(Comment::getCreateTime);
        
        IPage<Comment> commentIPage = commentMapper.selectPage(commentPage, wrapper);
        
        Long currentUserId = securityUtil.getCurrentUserId();
        IPage<CommentDTO> dtoPage = new Page<>(commentIPage.getCurrent(), commentIPage.getSize(), commentIPage.getTotal());
        List<CommentDTO> dtoList = commentIPage.getRecords().stream()
                .map(comment -> convertCommentToDTO(comment, currentUserId))
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Override
    public List<CommentDTO> getUserComments(Long userId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getAuthorId, userId);
        wrapper.orderByDesc(Comment::getCreateTime);
        
        List<Comment> comments = commentMapper.selectList(wrapper);
        Long currentUserId = securityUtil.getCurrentUserId();
        
        return comments.stream()
                .map(comment -> convertCommentToDTO(comment, currentUserId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReplyDTO> getRepliesByCommentId(Long commentId) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return buildReplyTree(commentId, currentUserId);
    }
    
    /**
     * 将Comment实体转换为CommentDTO，并填充回复树形结构
     */
    private CommentDTO convertCommentToDTO(Comment comment, Long currentUserId) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getResourceId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setCreateTime(comment.getCreateTime());
        
        // 查询作者信息
        User author = userMapper.selectById(comment.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatar());
        }
        
        // 检查是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Like::getCommentId, comment.getId());
            likeWrapper.eq(Like::getUserId, currentUserId);
            dto.setIsLiked(likeMapper.selectOne(likeWrapper) != null);
        } else {
            dto.setIsLiked(false);
        }
        
        // 只计算回复数量，不加载子评论（子评论在展开时单独加载）
        LambdaQueryWrapper<ForumReply> replyCountWrapper = new LambdaQueryWrapper<>();
        replyCountWrapper.eq(ForumReply::getCommentId, comment.getId());
        replyCountWrapper.isNull(ForumReply::getParentId); // 只统计直接回复评论的回复
        Long replyCount = replyMapper.selectCount(replyCountWrapper);
        dto.setReplyCount(replyCount != null ? replyCount.intValue() : 0);
        
        // 不设置 replies，让前端在展开时单独加载
        dto.setReplies(null);
        
        return dto;
    }
    
    /**
     * 构建回复的扁平结构（所有回复都是根评论的直接子回复，只支持两层）
     */
    private List<ReplyDTO> buildReplyTree(Long commentId, Long currentUserId) {
        // 查询该评论的所有回复（只返回 parentId 为 null 的回复，即直接回复评论的回复）
        LambdaQueryWrapper<ForumReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumReply::getCommentId, commentId);
        wrapper.isNull(ForumReply::getParentId); // 只获取直接回复评论的回复
        wrapper.orderByAsc(ForumReply::getCreateTime);
        List<ForumReply> allReplies = replyMapper.selectList(wrapper);
        
        if (allReplies.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 将所有回复转换为DTO（扁平结构，不嵌套）
        List<ReplyDTO> replyList = new ArrayList<>();
        for (ForumReply reply : allReplies) {
            ReplyDTO replyDTO = convertReplyToDTO(reply, currentUserId);
            replyList.add(replyDTO);
        }
        
        return replyList;
    }
    
    /**
     * 将ForumReply实体转换为ReplyDTO
     */
    private ReplyDTO convertReplyToDTO(ForumReply reply, Long currentUserId) {
        ReplyDTO dto = new ReplyDTO();
        dto.setId(reply.getId());
        dto.setCommentId(reply.getCommentId());
        dto.setParentId(reply.getParentId());
        dto.setAuthorId(reply.getAuthorId());
        dto.setTargetUserId(reply.getTargetUserId());
        dto.setContent(reply.getContent());
        dto.setLikeCount(reply.getLikeCount());
        dto.setCreateTime(reply.getCreateTime());
        
        // 查询作者信息
        User author = userMapper.selectById(reply.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatar());
        }
        
        // 查询被回复用户信息
        if (reply.getTargetUserId() != null) {
            User targetUser = userMapper.selectById(reply.getTargetUserId());
            if (targetUser != null) {
                dto.setTargetUserName(targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername());
            }
        }
        
        // 检查是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Like::getReplyId, reply.getId());
            likeWrapper.eq(Like::getUserId, currentUserId);
            dto.setIsLiked(likeMapper.selectOne(likeWrapper) != null);
        } else {
            dto.setIsLiked(false);
        }
        
        dto.setChildren(new ArrayList<>());
        
        return dto;
    }
}





