package com.github.ltsopensource.monitor.access.face;

import com.github.ltsopensource.monitor.access.domain.JobTrackerMDataPo;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface JobTrackerMAccess {

    void insert(List<JobTrackerMDataPo> jobTrackerMDataPos);

}
