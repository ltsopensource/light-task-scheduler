package com.lts.job.core.protocol.command;

import com.lts.job.core.Application;

/**
 * 用于设置CommandBody 的基础信息
 * Robert HG (254963746@qq.com) on 3/13/15.
 */
public class CommandBodyWrapper {

    private Application application;

    public CommandBodyWrapper(Application application) {
        this.application = application;
    }

    public <T extends AbstractCommandBody> T wrapper(T commandBody) {
        commandBody.setNodeGroup(application.getConfig().getNodeGroup());
        commandBody.setNodeType(application.getConfig().getNodeType().name());
        commandBody.setIdentity(application.getConfig().getIdentity());
        return commandBody;
    }

}
