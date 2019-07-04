package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.spring.quartz.invoke.JobExecution;
import java.util.Map;
import lombok.Data;
import org.quartz.Trigger;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
@Data
public class QuartzJobContext {

    private String name;

    private QuartzJobType type;

    private Trigger trigger;

    private JobExecution jobExecution;

    private Map<String, Object> jobDataMap;

}
