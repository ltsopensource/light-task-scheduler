package com.lts.job.client;

import com.lts.job.client.processor.RemotingDispatcher;
import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.common.cluster.AbstractClientNode;
import com.lts.job.common.constant.Constants;
import com.lts.job.common.domain.Job;
import com.lts.job.client.domain.JobClientNode;
import com.lts.job.client.domain.Response;
import com.lts.job.client.domain.ResponseCode;
import com.lts.job.common.cluster.NodeType;
import com.lts.job.common.exception.JobTrackerNotFoundException;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.JobSubmitRequest;
import com.lts.job.common.protocol.command.JobSubmitResponse;
import com.lts.job.common.util.BatchUtils;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 * 任务客户端
 */
public class JobClient<T extends JobClientNode> extends AbstractClientNode<JobClientNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger("JobClient");

    private static final int BATCH_SIZE = 50;

    private JobFinishedHandler jobFinishedHandler;

    public JobClient() {
        // 设置默认节点组
        config.setNodeGroup(Constants.DEFAULT_NODE_JOB_CLIENT_GROUP);
    }

    /**
     * @param job
     * @return
     */
    public Response submitJob(Job job) {
        return submitJob(Arrays.asList(job));
    }

    protected Response _submitJob(List<Job> jobs) {
        Response response = new Response();

        try {
            JobSubmitRequest jobSubmitRequest = new JobSubmitRequest();
            jobSubmitRequest.setJobs(jobs);

            RemotingCommand requestCommand = RemotingCommand.createRequestCommand(JobProtos.RequestCode.SUBMIT_JOB.code(), jobSubmitRequest);

            // 同步调用
            RemotingCommand responseCommand = remotingClient.invokeSync(requestCommand);
            JobSubmitResponse jobSubmitResponse = responseCommand.getBody();

            if (JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code() == responseCommand.getCode()) {

                LOGGER.info("提交任务成功: " + jobs);
                response.setSuccess(true);

            } else {
                // 失败的job
                response.setFailedJobs(jobSubmitResponse.getFailedJobs());
                response.setSuccess(false);
                response.setCode(JobProtos.ResponseCode.valueOf(responseCommand.getCode()).name());
                LOGGER.warn("提交任务失败: " + jobs + ", " + responseCommand.getRemark() + " " + jobSubmitResponse.getMsg());
            }

        } catch (RemotingCommandFieldCheckException e) {
            response.setCode(ResponseCode.REQUEST_FILED_CHECK_ERROR);
            response.setMsg("the request body's field check error : " + e.getMessage());
        } catch (JobTrackerNotFoundException e) {
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("can not found JobTracker node!");
        }

        return response;
    }

    /**
     * @param jobs
     * @return
     */
    public Response submitJob(List<Job> jobs) {

        Response response = new Response();
        response.setSuccess(true);

        for (int i = 0; i <= jobs.size() / BATCH_SIZE; i++) {
            List<Job> subJobs = BatchUtils.getBatchList(i, BATCH_SIZE, jobs);

            if (subJobs != null && subJobs.size() > 0) {
                Response subResponse = _submitJob(subJobs);
                if (!subResponse.isSuccess()) {
                    response.setSuccess(false);
                    response.addFailedJobs(subJobs);
                    response.setMsg(subResponse.getMsg());
                }
            }
        }

        return response;
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingClient, jobFinishedHandler);
    }

    /**
     * 设置任务完成接收器
     * @param jobFinishedHandler
     */
    public void setJobFinishedHandler(JobFinishedHandler jobFinishedHandler) {
        this.jobFinishedHandler = jobFinishedHandler;
    }
}
