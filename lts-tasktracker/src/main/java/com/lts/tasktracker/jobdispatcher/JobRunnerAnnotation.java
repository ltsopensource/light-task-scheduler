package com.lts.tasktracker.jobdispatcher;

import java.lang.annotation.*;

/**
 * 自动扫描作业注解，根据参数 type 派发作业
 *
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobRunnerAnnotation {

    /**
     * 作业类型，JobRunnerDispatcher 负责扫描分发
     */
    String type();

}
