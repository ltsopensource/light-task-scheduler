package com.github.ltsopensource.spring.tasktracker;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Robert HG (254963746@qq.com)on 12/21/15.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface LTS {
}
