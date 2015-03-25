package com.lts.job.core.protocol.command;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.support.Application;

/**
 * 用于设置CommandBody 的基础信息
 * Robert HG (254963746@qq.com) on 3/13/15.
 */
public class CommandWrapper {

    private Application application;

    public CommandWrapper(Application application) {
        this.application = application;
    }

    public <T extends AbstractCommandBody> T wrapper(T commandBody) {
        commandBody.setNodeGroup(application.getConfig().getNodeGroup());
        commandBody.setNodeType(application.getConfig().getNodeType().name());
        commandBody.setIdentity(application.getConfig().getIdentity());
        return commandBody;
    }

}
