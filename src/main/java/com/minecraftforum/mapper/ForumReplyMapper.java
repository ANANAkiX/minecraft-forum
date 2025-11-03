package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.ForumReply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForumReplyMapper extends BaseMapper<ForumReply> {
}

