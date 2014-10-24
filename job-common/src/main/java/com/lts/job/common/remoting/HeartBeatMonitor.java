package com.lts.job.common.remoting;

import com.lts.job.common.exception.JobTrackerNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 * 心跳监控。 如果心跳失败，连不上server，那么发现并连接新的server, 如果server集群中，没有可用的server, 那么报警
 */
public class HeartBeatMonitor {

    private static final Log LOGGER = LogFactory.getLog("HeartBeat");

    // 用来定时发送心跳
    private final ScheduledExecutorService HEART_BEAT_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private RemotingClientDelegate remotingClient;

    public HeartBeatMonitor(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }

    public void start() {
        HEART_BEAT_EXECUTOR_SERVICE.scheduleWithFixedDelay(new HeartBeatRunner(), 5, 10, TimeUnit.SECONDS);  // 10s发送一次心跳
    }

    public void destroy() {
        HEART_BEAT_EXECUTOR_SERVICE.shutdown();
    }

    private class HeartBeatRunner implements Runnable {

        @Override
        public void run() {
            try {
                String addr = remotingClient.getStickyJobTrackerNode().getAddress();
                if (!HeartBeater.beat(remotingClient, addr)) {
                    remotingClient.changeStickyJobTrackerNode();
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    LOGGER.warn("发送心跳给" + addr + "失败!");
                    run();
                } else {
                    if(LOGGER.isDebugEnabled()){
                        LOGGER.debug("发送心跳给" + addr + "成功!");
                    }
                    remotingClient.setServerEnable(true);
                }
            } catch (JobTrackerNotFoundException e) {
                remotingClient.setServerEnable(false);
            } catch (Throwable t) {
                LOGGER.error(t.getMessage(), t);
            }
        }

    }
}
