package com.lts.queue.mongo;

import com.lts.core.AppContext;
import com.lts.core.cluster.Config;
import com.lts.queue.*;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class MongoJobQueueFactory implements JobQueueFactory {
    @Override
    public CronJobQueue getCronJobQueue(Config config) {
        return new MongoCronJobQueue(config);
    }

    @Override
    public RepeatJobQueue getRepeatJobQueue(Config config) {
        return new MongoRepeatJobQueue(config);
    }

    @Override
    public ExecutableJobQueue getExecutableJobQueue(Config config) {
        return new MongoExecutableJobQueue(config);
    }

    @Override
    public ExecutingJobQueue getExecutingJobQueue(Config config) {
        return new MongoExecutingJobQueue(config);
    }

    @Override
    public JobFeedbackQueue getJobFeedbackQueue(Config config) {
        return new MongoJobFeedbackQueue(config);
    }

    @Override
    public NodeGroupStore getNodeGroupStore(Config config) {
        return new MongoNodeGroupStore(config);
    }

    @Override
    public SuspendJobQueue getSuspendJobQueue(Config config) {
        return new MongoSuspendJobQueue(config);
    }

    @Override
    public PreLoader getPreLoader(AppContext appContext) {
        return new MongoPreLoader(appContext);
    }
}
