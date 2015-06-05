package com.lts.job.tracker.id;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;
import com.lts.job.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
@SPI("md5")
public interface IdGenerator {

    /**
     * 生成ID
     * @param jobPo
     * @return
     */
    @Adaptive("id.generator")
    public String generate(Config config, JobPo jobPo);

}
