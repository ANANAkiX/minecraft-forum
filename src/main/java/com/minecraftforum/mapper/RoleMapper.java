package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}

