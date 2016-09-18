package com.github.ltsopensource.monitor.access.face;

import com.github.ltsopensource.monitor.access.domain.TaskTrackerMDataPo;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/22/15.
 */
public interface TaskTrackerMAccess {

    void insert(List<TaskTrackerMDataPo> pos);

}
