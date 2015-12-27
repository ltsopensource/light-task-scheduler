package com.lts.jobtracker.id;

import com.lts.core.commons.utils.Md5Encrypt;
import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class Md5Generator implements IdGenerator{
    @Override
    public String generate(JobPo jobPo) {
        return Md5Encrypt.md5(jobPo.getTaskId() + jobPo.getSubmitNodeGroup() + jobPo.getGmtCreated());
    }
}
