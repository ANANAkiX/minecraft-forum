package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}

