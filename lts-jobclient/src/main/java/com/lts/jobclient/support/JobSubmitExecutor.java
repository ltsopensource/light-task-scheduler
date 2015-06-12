package com.lts.jobclient.support;

import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public interface JobSubmitExecutor<T> {

    T execute(List<Job> jobs) throws JobSubmitException;

}
