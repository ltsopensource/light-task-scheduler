package com.lts.job.core.remoting;

import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.HeartBeatRequest;
import com.lts.job.core.support.Application;
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

        HeartBeatRequest commandBody = remotingClient.getApplication().getCommandWrapper().wrapper(new HeartBeatRequest());

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.HEART_BEAT.code(), commandBody);
        RemotingCommand response = null;
        try {
            response = remotingClient.getNettyClient().invokeSync(addr, request, remotingClient.getApplication().getConfig().getInvokeTimeoutMillis());
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
