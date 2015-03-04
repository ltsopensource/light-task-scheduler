package com.lts.job.core.protocol.command;

import com.lts.job.core.domain.Job;
import com.lts.job.core.support.CronExpression;
import com.lts.job.remoting.annotation.NotNull;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         任务传递信息
 */
public class JobSubmitRequest extends AbstractCommandBody {

    @NotNull
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {
        super.checkFields();

        for (Job job : jobs) {
            if (job.isSchedule()) {
                if (!CronExpression.isValidExpression(job.getCronExpression())) {
                    throw new RemotingCommandFieldCheckException("cronExpression表达式错误, " + job);
                }
            }
        }
    }
}
