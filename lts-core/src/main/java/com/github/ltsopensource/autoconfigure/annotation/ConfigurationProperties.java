package com.github.ltsopensource.autoconfigure.annotation;

import java.lang.annotation.*;

/**
 * 和SpringBoot的类似
 * @author Robert HG (254963746@qq.com) on 4/18/16.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

    /**
     * 前缀
     */
    String prefix() default "";

    /**
     * 直接指定文件位置
     */
    String[] locations() default {};
}
