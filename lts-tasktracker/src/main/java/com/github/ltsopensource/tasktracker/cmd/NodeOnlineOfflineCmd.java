package com.github.ltsopensource.tasktracker.cmd;

import com.github.ltsopensource.cmd.HttpCmdProc;
import com.github.ltsopensource.cmd.HttpCmdRequest;
import com.github.ltsopensource.cmd.HttpCmdResponse;
import com.github.ltsopensource.core.cmd.HttpCmdNames;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

/**
 * 节点上下线
 *
 * @author Robert HG (254963746@qq.com) on 29/03/2017.
 */
public class NodeOnlineOfflineCmd implements HttpCmdProc {

    private TaskTrackerAppContext appContext;

    public NodeOnlineOfflineCmd(TaskTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_NODE_ONLINE_OFFLINE;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {


        return null;
    }
}
