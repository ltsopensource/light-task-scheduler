package com.lts.job.biz.logger.mongo;


import com.lts.job.biz.logger.JobLogUtils;
import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.LogType;
import com.lts.job.core.cluster.Config;
import com.lts.job.store.mongo.AbstractMongoRepository;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobLogger extends AbstractMongoRepository<JobLogPo> implements JobLogger {

    public MongoJobLogger(Config config) {
        super(config);
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        ds.save(jobLogPo);
    }

    @Override
    public void log(BizLogPo bizLogPo) {
        ds.save(JobLogUtils.bizConvert(bizLogPo));
    }

}
