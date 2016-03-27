package com.lts.queue.domain;

import java.io.Serializable;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class JobEntry implements Serializable {

    private String taskId;
    private String taskTrackerNodeGroup;

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobEntry jobEntry = (JobEntry) o;

        if (taskId != null ? !taskId.equals(jobEntry.taskId) : jobEntry.taskId != null) return false;
        return taskTrackerNodeGroup != null ? taskTrackerNodeGroup.equals(jobEntry.taskTrackerNodeGroup) : jobEntry.taskTrackerNodeGroup == null;

    }

    @Override
    public int hashCode() {
        int result = taskId != null ? taskId.hashCode() : 0;
        result = 31 * result + (taskTrackerNodeGroup != null ? taskTrackerNodeGroup.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobEntry{" +
                "taskId='" + taskId + '\'' +
                ", taskTrackerNodeGroup='" + taskTrackerNodeGroup + '\'' +
                '}';
    }
}
