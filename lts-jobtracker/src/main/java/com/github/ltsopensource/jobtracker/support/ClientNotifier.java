package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.core.domain.JobRunResult;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/2/15.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public interface ClientNotifier {

    <T extends JobRunResult> int send(List<T> jobResults);
}
