package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.concurrent.limiter.RateLimiter;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.AbstractRemotingCommandBody;
import com.github.ltsopensource.jobtracker.channel.ChannelWrapper;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.ltsopensource.core.protocol.JobProtos.RequestCode;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 *         job tracker 总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractRemotingProcessor {

    private final Map<RequestCode, RemotingProcessor> processors = new HashMap<RequestCode, RemotingProcessor>();
    private RateLimiter rateLimiter;
    private int reqLimitAcquireTimeout = 50;
    private boolean reqLimitEnable = false;

    public RemotingDispatcher(JobTrackerAppContext appContext) {
        super(appContext);
        processors.put(RequestCode.SUBMIT_JOB, new JobSubmitProcessor(appContext));
        processors.put(RequestCode.JOB_COMPLETED, new JobCompletedProcessor(appContext));
        processors.put(RequestCode.JOB_PULL, new JobPullProcessor(appContext));
        processors.put(RequestCode.BIZ_LOG_SEND, new JobBizLogProcessor(appContext));
        processors.put(RequestCode.CANCEL_JOB, new JobCancelProcessor(appContext));

        this.reqLimitEnable = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_REMOTING_REQ_LIMIT_ENABLE, false);
        Integer maxQPS = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_REMOTING_REQ_LIMIT_MAX_QPS, 5000);
        this.rateLimiter = RateLimiter.create(maxQPS);
        this.reqLimitAcquireTimeout = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_REMOTING_REQ_LIMIT_ACQUIRE_TIMEOUT, 50);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {
        // 心跳
        if (request.getCode() == JobProtos.RequestCode.HEART_BEAT.code()) {
            offerHandler(channel, request);
            return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.HEART_BEAT_SUCCESS.code(), "");
        }
        if (reqLimitEnable) {
            return doBizWithReqLimit(channel, request);
        } else {
            return doBiz(channel, request);
        }
    }

    /**
     * 限流处理
     */
    private RemotingCommand doBizWithReqLimit(Channel channel, RemotingCommand request) throws RemotingCommandException {

        if (rateLimiter.tryAcquire(reqLimitAcquireTimeout, TimeUnit.MILLISECONDS)) {
            return doBiz(channel, request);
        }
        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_BUSY.code(), "remoting server is busy!");
    }

    private RemotingCommand doBiz(Channel channel, RemotingCommand request) throws RemotingCommandException {
        // 其他的请求code
        RequestCode code = RequestCode.valueOf(request.getCode());
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(), "request code not supported!");
        }
        offerHandler(channel, request);
        return processor.processRequest(channel, request);
    }

    /**
     * 1. 将 channel 纳入管理中(不存在就加入)
     * 2. 更新 TaskTracker 节点信息(可用线程数)
     */
    private void offerHandler(Channel channel, RemotingCommand request) {
        AbstractRemotingCommandBody commandBody = request.getBody();
        String nodeGroup = commandBody.getNodeGroup();
        String identity = commandBody.getIdentity();
        NodeType nodeType = NodeType.valueOf(commandBody.getNodeType());

        // 1. 将 channel 纳入管理中(不存在就加入)
        appContext.getChannelManager().offerChannel(new ChannelWrapper(channel, nodeType, nodeGroup, identity));
    }

}
