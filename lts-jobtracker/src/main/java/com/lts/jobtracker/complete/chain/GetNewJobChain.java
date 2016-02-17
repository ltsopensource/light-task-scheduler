package com.lts.jobtracker.complete.chain;

import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.core.protocol.command.JobPushRequest;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.jobtracker.sender.JobSender;
import com.lts.jobtracker.support.JobDomainConverter;
import com.lts.queue.domain.JobPo;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;

/**
 * 接受新任务Chain
 *
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class GetNewJobChain implements JobCompletedChain {

    private JobTrackerAppContext appContext;

    public GetNewJobChain(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public RemotingCommand doChain(JobCompletedRequest request) {
        // 判断是否接受新任务
        if (request.isReceiveNewJob()) {
            try {
                // 查看有没有其他可以执行的任务
                JobPushRequest jobPushRequest = getNewJob(request.getNodeGroup(), request.getIdentity());
                // 返回 新的任务
                return RemotingCommand.createResponseCommand(RemotingProtos
                        .ResponseCode.SUCCESS.code(), jobPushRequest);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 获取新任务去执行
     */
    private JobPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        JobSender.SendResult sendResult = appContext.getJobSender().send(taskTrackerNodeGroup, taskTrackerIdentity, new JobSender.SendInvoker() {
            @Override
            public JobSender.SendResult invoke(JobPo jobPo) {

                JobPushRequest jobPushRequest = appContext.getCommandBodyWrapper().wrapper(new JobPushRequest());
                jobPushRequest.setJobWrapper(JobDomainConverter.convert(jobPo));

                return new JobSender.SendResult(true, jobPushRequest);
            }
        });

        if (sendResult.isSuccess()) {
            return (JobPushRequest) sendResult.getReturnValue();
        }
        return null;
    }
}
