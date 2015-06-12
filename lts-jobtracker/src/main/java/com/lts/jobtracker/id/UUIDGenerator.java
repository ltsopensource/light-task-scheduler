package com.lts.jobtracker.id;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class UUIDGenerator implements IdGenerator{
    @Override
    public String generate(Config config, JobPo jobPo) {
        return StringUtils.generateUUID();
    }
}
