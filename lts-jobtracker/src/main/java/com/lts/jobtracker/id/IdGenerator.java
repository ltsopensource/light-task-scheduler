package com.lts.jobtracker.id;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;
import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
@SPI("md5")
public interface IdGenerator {

    /**
     * 生成ID
     */
    @Adaptive("id.generator")
    public String generate(Config config, JobPo jobPo);

}
