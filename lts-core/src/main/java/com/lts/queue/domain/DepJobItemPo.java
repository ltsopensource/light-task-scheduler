package com.lts.queue.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJobItemPo extends JobPo implements Serializable {

    private List<JobEntry> depJobs;

    private List<JobEntry> beDepJobs;

    public List<JobEntry> getDepJobs() {
        return depJobs;
    }

    public void setDepJobs(List<JobEntry> depJobs) {
        this.depJobs = depJobs;
    }

    public List<JobEntry> getBeDepJobs() {
        return beDepJobs;
    }

    public void setBeDepJobs(List<JobEntry> beDepJobs) {
        this.beDepJobs = beDepJobs;
    }
}
