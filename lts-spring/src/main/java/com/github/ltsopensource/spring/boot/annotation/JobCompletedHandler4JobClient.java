package com.github.ltsopensource.spring.boot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于JobClient的任务完成处理器
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobCompletedHandler4JobClient {

}
