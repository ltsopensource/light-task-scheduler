package com.github.ltsopensource.monitor.access.face;

import com.github.ltsopensource.monitor.access.domain.JobClientMDataPo;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public interface JobClientMAccess {

    void insert(List<JobClientMDataPo> jobTrackerMDataPos);

}
