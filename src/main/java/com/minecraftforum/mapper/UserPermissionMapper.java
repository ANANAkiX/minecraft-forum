package com.minecraftforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minecraftforum.entity.UserPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPermissionMapper extends BaseMapper<UserPermission> {
}

