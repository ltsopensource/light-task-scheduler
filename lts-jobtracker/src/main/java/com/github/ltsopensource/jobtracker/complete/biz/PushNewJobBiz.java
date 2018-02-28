package com.github.ltsopensource.jobtracker.complete.biz;

import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.core.protocol.command.JobPushRequest;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.sender.JobSender;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;

import java.util.List;

/**
 * 接受新任务Chain
 *
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class PushNewJobBiz implements JobCompletedBiz {

    private JobTrackerAppContext appContext;

    public PushNewJobBiz(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public RemotingCommand doBiz(JobCompletedRequest request) {
        // 判断是否接受新任务
        if (request.isReceiveNewJob()) {
            try {
                // 查看有没有其他可以执行的任务
                JobPushRequest jobPushRequest = getNewJob(request.getNodeGroup(), request.getIdentity());
                // 返回 新的任务
                return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), jobPushRequest);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 获取新任务去执行
     */
    private JobPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        JobSender.SendResult sendResult = appContext.getJobSender().send(taskTrackerNodeGroup, taskTrackerIdentity, 1, new JobSender.SendInvoker() {
            @Override
            public JobSender.SendResult invoke(List<JobPo> jobPos) {

                JobPushRequest jobPushRequest = appContext.getCommandBodyWrapper().wrapper(new JobPushRequest());
                jobPushRequest.setJobMetaList(JobDomainConverter.convert(jobPos));

                return new JobSender.SendResult(true, jobPushRequest);
            }
        });

        if (sendResult.isSuccess()) {
            return (JobPushRequest) sendResult.getReturnValue();
        }
        return null;
    }
}
