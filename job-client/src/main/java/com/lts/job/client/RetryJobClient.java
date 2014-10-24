package com.lts.job.client;

import com.lts.job.client.domain.JobClientNode;
import com.lts.job.client.domain.ResponseCode;
import com.lts.job.common.domain.Job;
import com.lts.job.client.domain.Response;
import com.lts.job.common.file.FileAccessor;
import com.lts.job.common.file.FileException;
import com.lts.job.common.file.Line;
import com.lts.job.common.support.RetryScheduler;
import com.lts.job.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * 重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends JobClient<JobClientNode> {

    private RetryScheduler retryScheduler;
    private FileAccessor fileAccessor;

    @Override
    protected void nodeStart() {

        retryScheduler = new RetryScheduler<Job>(30) {
            @Override
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            @Override
            protected boolean retry(List<Job> jobs) {
                return submitJob(jobs).isSuccess();
            }
        };
        fileAccessor = retryScheduler.getFileAccessor();

        super.nodeStart();
        retryScheduler.start();
    }

    @Override
    protected void nodeStop() {
        super.nodeStop();
        retryScheduler.stop();
    }

    @Override
    public Response submitJob(Job job) {
        return submitJob(Arrays.asList(job));
    }

    @Override
    public Response submitJob(List<Job> jobs) {
        Response response = super.submitJob(jobs);

        if (!response.isSuccess()) {
            // 存储文件
            List<Line> lines = new ArrayList<Line>();
            for (Job job : response.getFailedJobs()) {
                String line = JsonUtils.objectToJsonString(job);
                lines.add(new Line(line));
            }

            try {
                if (fileAccessor == null) {
                    throw new RuntimeException("save file error ! can not get file accessor !");
                }
                fileAccessor.addLines(lines);
                response.setSuccess(true);
                response.setCode(ResponseCode.FAILED_AND_SAVE_FILE);

            } catch (FileException e) {
                response.setSuccess(false);
                response.setMsg(e.getMessage());
            }
        }

        return response;
    }

}
