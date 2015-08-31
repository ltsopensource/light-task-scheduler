package com.lts.jobtracker.processor;

import com.lts.core.cluster.NodeType;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.AbstractCommandBody;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.jobtracker.channel.ChannelWrapper;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import static com.lts.core.protocol.JobProtos.RequestCode;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 *         job tracker 总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractProcessor {

    private final Map<RequestCode, NettyRequestProcessor> processors = new HashMap<RequestCode, NettyRequestProcessor>();

    public RemotingDispatcher(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
        processors.put(RequestCode.SUBMIT_JOB, new JobSubmitProcessor(remotingServer, application));
        processors.put(RequestCode.JOB_FINISHED, new JobFinishedProcessor(remotingServer, application));
        processors.put(RequestCode.JOB_PULL, new JobPullProcessor(remotingServer, application));
        processors.put(RequestCode.BIZ_LOG_SEND, new JobBizLogProcessor(remotingServer, application));
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
        // 心跳
        if (request.getCode() == JobProtos.RequestCode.HEART_BEAT.code()) {
            commonHandler(ctx, request);
            return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.HEART_BEAT_SUCCESS.code(), "");
        }

        // 其他的请求code
        RequestCode code = RequestCode.valueOf(request.getCode());
        NettyRequestProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(), "request code not supported!");
        }
        commonHandler(ctx, request);
        return processor.processRequest(ctx, request);
    }

    /**
     * 1. 将 channel 纳入管理中(不存在就加入)
     * 2. 更新 TaskTracker 节点信息(可用线程数)
     */
    private void commonHandler(ChannelHandlerContext ctx, RemotingCommand request) {
        AbstractCommandBody commandBody = request.getBody();
        String nodeGroup = commandBody.getNodeGroup();
        String identity = commandBody.getIdentity();
        NodeType nodeType = NodeType.valueOf(commandBody.getNodeType());

        // 1. 将 channel 纳入管理中(不存在就加入)
        application.getChannelManager().offerChannel(new ChannelWrapper(ctx.channel(), nodeType, nodeGroup, identity));
    }

}
