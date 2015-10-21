package com.lts.spring.tasktracker;

import java.lang.annotation.*;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LtsJobRunner {

    /**
     * shardField 的值
     */
    String value() default "";

}
