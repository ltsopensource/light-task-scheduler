package com.lts.job.tracker.processor;

import com.lts.job.common.cluster.NodeType;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.HeartBeatRequest;
import com.lts.job.common.remoting.RemotingServerDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.support.JobController;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 心跳处理器
 */
public class HeartBeatProcessor extends AbstractProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(HeartBeatProcessor.class);

    private final ExecutorService executor;

    public HeartBeatProcessor(RemotingServerDelegate remotingServer) {
        super(remotingServer);

        executor = Executors.newCachedThreadPool();
    }

    @Override
    public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request) throws RemotingCommandException {

        HeartBeatRequest requestBody = request.getBody();

        NodeType nodeType = NodeType.valueOf(requestBody.getNodeType());

        // 1. 分发任务给TaskTracker
        if (NodeType.TASK_TRACKER.equals(nodeType)) {

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        JobController.pushJob(remotingServer, ctx, request);
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                    }
                }
            });

        }
        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.HEART_BEAT_SUCCESS.code(), "heart beat received success!");
    }
}
