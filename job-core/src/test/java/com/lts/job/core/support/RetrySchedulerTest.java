package com.lts.job.core.support;

import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobNodeConfig;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RetrySchedulerTest {

    @Test
    public void testInSchedule() throws Exception {

        JobNodeConfig config = new JobNodeConfig();
        config.setJobInfoSavePath(Constants.USER_HOME + "/.job");
        config.setNodeType(NodeType.CLIENT);
        config.setNodeGroup("TEST");
        Application.Config = config;

        RetryScheduler<Job> retryScheduler = new RetryScheduler<Job>() {
            @Override
            protected boolean isRemotingEnable() {
                return true;
            }

            @Override
            protected boolean retry(List<Job> list) {
                for (Job job : list) {
                    System.out.println(job);
                }
                return true;
            }
        };

        retryScheduler.inSchedule("11111", "232323");
    }
}