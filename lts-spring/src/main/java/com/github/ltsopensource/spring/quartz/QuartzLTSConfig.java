package com.github.ltsopensource.spring.quartz;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
@Data
class QuartzLTSConfig {

    private JobProperties jobProperties;


    @Data
    @ConfigurationProperties(prefix = "lts.jobProp")
    public static class JobProperties {

        // 是否要反馈给客户端
        private Boolean needFeedback;
        // 该任务最大的重试次数
        private Integer maxRetryTimes;
        /**
         * 如果为true, 每次启动时, 则以本地为准, 覆盖lts上的 如果为false,每次启动是, 则以lts为准, 如果lts上已经存在, 则不添加
         */
        private Boolean replaceOnExist;
        /**
         * 是否依赖上一个执行周期(对于周期性任务才起作用)
         */
        private Boolean relyOnPrevCycle;
    }
}
