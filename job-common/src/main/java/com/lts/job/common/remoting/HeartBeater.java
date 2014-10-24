package com.lts.job.common.remoting;

import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.HeartBeatRequest;
import com.lts.job.common.support.Application;
import com.lts.job.remoting.protocol.RemotingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 * 心跳工具类
 */
public class HeartBeater {

    private HeartBeater() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("HeartBeat");

    /**
     * 发送心跳
     *
     * @param remotingClient
     * @param addr
     */
    public static boolean beat(RemotingClientDelegate remotingClient, String addr) {

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), new HeartBeatRequest());
        RemotingCommand response = null;
        try {
            response = remotingClient.getNettyClient().invokeSync(addr, request, Application.Config.getInvokeTimeoutMillis());
        } catch (Exception e) {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug(e.getMessage(), e);
            }
            return false;
        }

        if (response != null && JobProtos.ResponseCode.HEART_BEAT_SUCCESS == JobProtos.ResponseCode.valueOf(response.getCode())) {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("heart beat success! ");
            }
            return true;
        }
        LOGGER.error("heart beat error !" + response);
        return false;
    }
}
