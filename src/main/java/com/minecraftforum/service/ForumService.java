package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.CommentDTO;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.dto.ReplyDTO;
import com.minecraftforum.entity.Comment;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.ForumReply;

import java.util.List;

public interface ForumService {
    IPage<ForumPostDTO> getPostList(Page<ForumPost> page, String category, String keyword, String authorKeyword, String sortBy);
    ForumPostDTO getPostById(Long id);
    ForumPost createPost(ForumPost post);
    ForumPost updatePost(ForumPost post);
    void deletePost(Long id);
    void likePost(Long postId, Long userId);
    void unlikePost(Long postId, Long userId);
    void viewPost(Long postId);
    Comment createComment(Long postId, Long userId, String content);
    void deleteComment(Long commentId);
    ForumReply createReply(Long commentId, Long userId, String content, Long targetUserId, Long parentId);
    void deleteReply(Long replyId);
    void likeComment(Long commentId, Long userId);
    void unlikeComment(Long commentId, Long userId);
    void likeReply(Long replyId, Long userId);
    void unlikeReply(Long replyId, Long userId);
    IPage<CommentDTO> getCommentsByPostId(Long postId, Integer page, Integer pageSize);
    List<CommentDTO> getUserComments(Long userId);
    List<ReplyDTO> getRepliesByCommentId(Long commentId); // 获取某个评论的所有子评论
}






