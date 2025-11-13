package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}

