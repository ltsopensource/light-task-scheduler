package com.lts.example.springboot;

import com.lts.core.domain.Job;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.domain.Response;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hugui.hg on 4/9/16.
 */
@Component
public class JobClientReferenceBean implements InitializingBean{

    @Autowired
    private JobClient jobClient;

    @Override
    public void afterPropertiesSet() throws Exception {

        Job job = new Job();
        job.setTaskId("t_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression("0 0/1 * * * ?");
//        job.setTriggerTime(DateUtils.addDay(new Date(), 1));
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }
}
