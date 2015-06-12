package com.lts.jobtracker.id;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.Md5Encrypt;
import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class Md5Generator implements IdGenerator{
    @Override
    public String generate(Config config, JobPo jobPo) {
        StringBuilder sb = new StringBuilder();
        sb.append(jobPo.getTaskId())
                .append(jobPo.getSubmitNodeGroup())
                .append(jobPo.getGmtCreated());
        return Md5Encrypt.md5(sb.toString());
    }
}
