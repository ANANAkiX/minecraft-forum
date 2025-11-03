package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.entity.Comment;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.ForumReply;

public interface ForumService {
    IPage<ForumPost> getPostList(Page<ForumPost> page, String category, String keyword);
    ForumPost getPostById(Long id);
    ForumPost createPost(ForumPost post);
    ForumPost updatePost(ForumPost post);
    void deletePost(Long id);
    void likePost(Long postId, Long userId);
    void unlikePost(Long postId, Long userId);
    void viewPost(Long postId);
    Comment createComment(Long postId, Long userId, String content);
    void deleteComment(Long commentId);
    ForumReply createReply(Long commentId, Long userId, String content, Long targetUserId);
    void deleteReply(Long replyId);
    void likeComment(Long commentId, Long userId);
    void likeReply(Long replyId, Long userId);
}

