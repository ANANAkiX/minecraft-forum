package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer status; // 0-正常, 1-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 清除密码信息（用于返回给前端时调用，确保密码不会泄露）
     */
    public void clearPassword() {
        this.password = null;
    }
}












