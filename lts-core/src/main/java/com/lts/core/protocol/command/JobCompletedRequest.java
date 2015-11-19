package com.lts.core.protocol.command;

import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.remoting.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 * TaskTracker Job completed request command body
 */
public class JobCompletedRequest extends AbstractRemotingCommandBody {
	private static final long serialVersionUID = 3034213298501228160L;

	/**
     * 是否接受新任务
     */
    private boolean receiveNewJob = false;

    @NotNull
    private List<TaskTrackerJobResult> taskTrackerJobResults;

    // 是否是重发(重发是批量发)
    private boolean reSend = false;

    public boolean isReSend() {
        return reSend;
    }

    public void setReSend(boolean reSend) {
        this.reSend = reSend;
    }

    public boolean isReceiveNewJob() {
        return receiveNewJob;
    }

    public void setReceiveNewJob(boolean receiveNewJob) {
        this.receiveNewJob = receiveNewJob;
    }

    public List<TaskTrackerJobResult> getTaskTrackerJobResults() {
        return taskTrackerJobResults;
    }

    public void setTaskTrackerJobResults(List<TaskTrackerJobResult> taskTrackerJobResults) {
        this.taskTrackerJobResults = taskTrackerJobResults;
    }

    public void addJobResult(TaskTrackerJobResult taskTrackerJobResult) {
        if (taskTrackerJobResults == null) {
            taskTrackerJobResults = new ArrayList<TaskTrackerJobResult>();
        }
        taskTrackerJobResults.add(taskTrackerJobResult);
    }
}
