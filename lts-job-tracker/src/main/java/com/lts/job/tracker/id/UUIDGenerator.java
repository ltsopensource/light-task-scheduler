package com.lts.job.tracker.id;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.commons.utils.StringUtils;
import com.lts.job.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class UUIDGenerator implements IdGenerator{
    @Override
    public String generate(Config config, JobPo jobPo) {
        return StringUtils.generateUUID();
    }
}
