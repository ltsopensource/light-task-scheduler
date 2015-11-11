package com.lts.jobtracker.complete;

import com.lts.core.domain.TaskTrackerJobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobCompleteHandler {

    void onComplete(List<TaskTrackerJobResult> results);

}
