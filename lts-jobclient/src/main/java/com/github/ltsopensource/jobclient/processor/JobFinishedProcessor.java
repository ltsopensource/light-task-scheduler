package com.github.ltsopensource.jobclient.processor;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobFinishedRequest;
import com.github.ltsopensource.jobclient.domain.JobClientAppContext;
import com.github.ltsopensource.jobclient.support.JobClientMStatReporter;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobFinishedProcessor implements RemotingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class);

    private JobClientAppContext appContext;
    private JobClientMStatReporter stat;

    public JobFinishedProcessor(JobClientAppContext appContext) {
        this.appContext = appContext;
        this.stat = (JobClientMStatReporter) appContext.getMStatReporter();
    }

    @Override
    public RemotingCommand processRequest(Channel Channel, RemotingCommand request)
            throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();
        try {
            if (appContext.getJobCompletedHandler() != null) {
                appContext.getJobCompletedHandler().onComplete(requestBody.getJobResults());
                stat.incHandleFeedbackNum(CollectionUtils.sizeOf(requestBody.getJobResults()));
            }
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code(),
                "received successful");
    }
}
