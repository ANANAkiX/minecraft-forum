package com.minecraftforum.config.custom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引到 Elasticsearch 注解
 * 用于标记需要索引到 Elasticsearch 的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexToElasticsearch {
    
    /**
     * 索引类型：POST（帖子）或 RESOURCE（资源）
     */
    IndexType type();
    
    /**
     * 获取ID的参数名（默认为返回值，如果返回值是实体对象）
     * 或者指定方法参数名，如 "id"、"postId"、"resourceId"
     */
    String idParam() default "";
    
    /**
     * 是否在删除时也处理（删除索引）
     */
    boolean onDelete() default false;
    
    enum IndexType {
        POST,
        RESOURCE
    }
}

