package com.github.ltsopensource.core.protocol.command;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Config;

/**
 * 用于设置CommandBody 的基础信息
 * @author Robert HG (254963746@qq.com) on 3/13/15.
 */
public class CommandBodyWrapper {

    private Config config;

    public CommandBodyWrapper(Config config) {
        this.config = config;
    }

    public <T extends AbstractRemotingCommandBody> T wrapper(T commandBody) {
        commandBody.setNodeGroup(config.getNodeGroup());
        commandBody.setNodeType(config.getNodeType().name());
        commandBody.setIdentity(config.getIdentity());
        return commandBody;
    }

    public static <T extends AbstractRemotingCommandBody> T wrapper(AppContext appContext, T commandBody) {
        return appContext.getCommandBodyWrapper().wrapper(commandBody);
    }

}
