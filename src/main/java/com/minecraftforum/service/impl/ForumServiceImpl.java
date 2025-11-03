package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.entity.*;
import com.minecraftforum.mapper.*;
import com.minecraftforum.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {
    
    private final ForumPostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ForumReplyMapper replyMapper;
    private final LikeMapper likeMapper;
    private final UserMapper userMapper;
    
    @Override
    public IPage<ForumPost> getPostList(Page<ForumPost> page, String category, String keyword) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(ForumPost::getCategory, category);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ForumPost::getTitle, keyword)
                    .or().like(ForumPost::getContent, keyword));
        }
        
        wrapper.eq(ForumPost::getStatus, "NORMAL");
        wrapper.orderByDesc(ForumPost::getCreateTime);
        
        return postMapper.selectPage(page, wrapper);
    }
    
    @Override
    public ForumPost getPostById(Long id) {
        ForumPost post = postMapper.selectById(id);
        if (post != null && "NORMAL".equals(post.getStatus())) {
            viewPost(id);
        }
        return post;
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
        post.setStatus("DELETED");
        postMapper.updateById(post);
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
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
        }
    }
    
    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getPostId, postId);
        wrapper.eq(Like::getUserId, userId);
        likeMapper.delete(wrapper);
        
        ForumPost post = postMapper.selectById(postId);
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postMapper.updateById(post);
    }
    
    @Override
    @Transactional
    public void viewPost(Long postId) {
        ForumPost post = postMapper.selectById(postId);
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
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
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);
        
        return comment;
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        ForumPost post = postMapper.selectById(comment.getResourceId());
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postMapper.updateById(post);
        
        commentMapper.deleteById(commentId);
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
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
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
            reply.setLikeCount(reply.getLikeCount() + 1);
            replyMapper.updateById(reply);
        }
    }
}

