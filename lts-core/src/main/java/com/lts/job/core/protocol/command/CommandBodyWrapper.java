package com.lts.job.core.protocol.command;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Config;

/**
 * 用于设置CommandBody 的基础信息
 * Robert HG (254963746@qq.com) on 3/13/15.
 */
public class CommandBodyWrapper {

    private Config config;

    public CommandBodyWrapper(Config config) {
        this.config = config;
    }

    public <T extends AbstractCommandBody> T wrapper(T commandBody) {
        commandBody.setNodeGroup(config.getNodeGroup());
        commandBody.setNodeType(config.getNodeType().name());
        commandBody.setIdentity(config.getIdentity());
        return commandBody;
    }

    public static <T extends AbstractCommandBody> T wrapper(Application application, T commandBody) {
        return application.getCommandBodyWrapper().wrapper(commandBody);
    }

}
