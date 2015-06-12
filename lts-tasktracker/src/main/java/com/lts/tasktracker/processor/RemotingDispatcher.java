package com.lts.tasktracker.processor;

import com.lts.core.protocol.JobProtos;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import com.lts.tasktracker.domain.TaskTrackerApplication;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * task tracker 总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractProcessor {

    private final Map<JobProtos.RequestCode, NettyRequestProcessor> processors = new HashMap<JobProtos.RequestCode, NettyRequestProcessor>();

    public RemotingDispatcher(RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        super(remotingClient, application);
        processors.put(JobProtos.RequestCode.PUSH_JOB, new JobPushProcessor(remotingClient, application));
        processors.put(JobProtos.RequestCode.JOB_ASK, new JobAskProcessor(remotingClient, application));
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobProtos.RequestCode code = JobProtos.RequestCode.valueOf(request.getCode());
        NettyRequestProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                    "request code not supported!");
        }
        return processor.processRequest(ctx, request);
    }

}
