package com.lts.job.core.remoting;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.HeartBeatRequest;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         心跳监控。 如果心跳失败，连不上server，那么发现并连接新的server, 如果server集群中，没有可用的server, 那么报警
 */
public class HeartBeatMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatMonitor.class.getSimpleName());

    // 用来定时发送心跳
    private final ScheduledExecutorService HEART_BEAT_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private RemotingClientDelegate remotingClient;
    private Application application;

    public HeartBeatMonitor(RemotingClientDelegate remotingClient, Application application) {
        this.remotingClient = remotingClient;
        this.application = application;
    }

    public void start() {
        HEART_BEAT_EXECUTOR_SERVICE.scheduleWithFixedDelay(
                new HeartBeat(), 5, 30, TimeUnit.SECONDS);      // 30s 一次心跳

    }

    public void stop() {
        HEART_BEAT_EXECUTOR_SERVICE.shutdown();
    }

    private class HeartBeat implements Runnable {

        @Override
        public void run() {
            try {
                List<Node> jobTrackers = application.getNodeManager().getNodeList(NodeType.JOB_TRACKER);
                if (jobTrackers == null) {
                    return;
                }
                for (Node jobTracker : jobTrackers) {
                    // 每个JobTracker 都要发送心跳
                    if (beat(remotingClient, jobTracker.getAddress())) {
                        remotingClient.addJobTracker(jobTracker);
                        remotingClient.setServerEnable(true);
                    } else {
                        remotingClient.removeJobTracker(jobTracker);
                    }
                }
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }

        /**
         * 发送心跳
         *
         * @param remotingClient
         * @param addr
         */
        private boolean beat(RemotingClientDelegate remotingClient, String addr) {

            HeartBeatRequest commandBody = application.getCommandBodyWrapper().wrapper(new HeartBeatRequest());

            RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), commandBody);
            final boolean[] result = {false};
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                remotingClient.getNettyClient().invokeAsync(addr, request, application.getConfig().getInvokeTimeoutMillis(), new InvokeCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        try {
                            RemotingCommand response = responseFuture.getResponseCommand();

                            if (response != null && JobProtos.ResponseCode.HEART_BEAT_SUCCESS == JobProtos.ResponseCode.valueOf(response.getCode())) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("heart beat success! ");
                                }
                                result[0] = true;
                                return;
                            }
                            LOGGER.error("heart beat error !" + response);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result[0];
        }

    }


}
