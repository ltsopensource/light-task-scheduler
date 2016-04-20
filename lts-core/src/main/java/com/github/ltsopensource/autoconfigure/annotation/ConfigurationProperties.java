package com.github.ltsopensource.autoconfigure.annotation;

import java.lang.annotation.*;

/**
 * Created by hugui.hg on 4/18/16.
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
