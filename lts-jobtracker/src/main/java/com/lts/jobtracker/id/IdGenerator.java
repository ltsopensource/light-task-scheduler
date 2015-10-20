package com.lts.jobtracker.id;

import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public interface IdGenerator {

    /**
     * 生成ID
     */
    public String generate(JobPo jobPo);

}
