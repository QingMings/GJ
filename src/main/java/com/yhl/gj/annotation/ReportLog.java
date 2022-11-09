package com.yhl.gj.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE}) // 表示注解可用于方法参数、方法、类
@Retention(RetentionPolicy.RUNTIME) // 表示注解一直存在
public @interface ReportLog {
    String title() default "";
}
