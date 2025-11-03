package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

