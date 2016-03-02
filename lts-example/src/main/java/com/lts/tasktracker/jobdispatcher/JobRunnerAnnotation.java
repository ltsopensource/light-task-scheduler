package com.lts.tasktracker.jobdispatcher;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobRunnerAnnotation {

    /**
     * 作业类型，JobRunnerDispatcher 负责扫描分发
     */
    String type();

}
