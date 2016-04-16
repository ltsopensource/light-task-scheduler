package com.github.ltsopensource.queue.mysql;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.queue.*;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class MysqlJobQueueFactory implements JobQueueFactory {

    @Override
    public CronJobQueue getCronJobQueue(Config config) {
        return new MysqlCronJobQueue(config);
    }

    @Override
    public RepeatJobQueue getRepeatJobQueue(Config config) {
        return new MysqlRepeatJobQueue(config);
    }

    @Override
    public ExecutableJobQueue getExecutableJobQueue(Config config) {
        return new MysqlExecutableJobQueue(config);
    }

    @Override
    public ExecutingJobQueue getExecutingJobQueue(Config config) {
        return new MysqlExecutingJobQueue(config);
    }

    @Override
    public JobFeedbackQueue getJobFeedbackQueue(Config config) {
        return new MysqlJobFeedbackQueue(config);
    }

    @Override
    public NodeGroupStore getNodeGroupStore(Config config) {
        return new MysqlNodeGroupStore(config);
    }

    @Override
    public SuspendJobQueue getSuspendJobQueue(Config config) {
        return new MysqlSuspendJobQueue(config);
    }

    @Override
    public PreLoader getPreLoader(AppContext appContext) {
        return new MysqlPreLoader(appContext);
    }
}
