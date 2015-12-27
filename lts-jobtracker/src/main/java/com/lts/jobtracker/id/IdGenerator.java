package com.lts.jobtracker.id;

import com.lts.core.spi.SPI;
import com.lts.core.spi.SKey;
import com.lts.queue.domain.JobPo;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
@SPI(key = SKey.JOB_ID_GENERATOR, dftValue = "md5")
public interface IdGenerator {

    /**
     * 生成ID
     */
    public String generate(JobPo jobPo);

}
