package com.minecraftforum.config.custom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAccess {
    /**
     * 是否从 API 扫描中排除
     * true: 扫描时跳过此方法/类
     * false: 正常扫描（默认值）
     */
    boolean excludeFromScan() default false;
}