package com.lts.spring.tasktracker;

import java.lang.annotation.*;

/**
 * Created by hugui.hg on 12/21/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JobRunnerItem {

    String shardValue() default "";
}
