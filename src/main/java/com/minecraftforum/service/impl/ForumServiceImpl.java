package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.entity.*;
import com.minecraftforum.mapper.*;
import com.minecraftforum.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
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
    public void deletePost(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post != null) {
            post.setStatus("DELETED");
            postMapper.updateById(post);
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
            ForumPost post = postMapper.selectById(comment.getResourceId());
            if (post != null) {
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                postMapper.updateById(post);
            }
            commentMapper.deleteById(commentId);
        }
    }
    
    @Override
    public ForumReply createReply(Long commentId, Long userId, String content, Long targetUserId) {
        ForumReply reply = new ForumReply();
        reply.setCommentId(commentId);
        reply.setAuthorId(userId);
        reply.setTargetUserId(targetUserId);
        reply.setContent(content);
        reply.setLikeCount(0);
        reply.setCreateTime(LocalDateTime.now());
        replyMapper.insert(reply);
        return reply;
    }
    
    @Override
    public void deleteReply(Long replyId) {
        replyMapper.deleteById(replyId);
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
}





