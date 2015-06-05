package com.lts.job.biz.logger;

import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.LogType;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobLogUtils {

    public static JobLogPo bizConvert(BizLogPo bizLogPo) {
        if (bizLogPo == null) {
            return null;
        }
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setTimestamp(bizLogPo.getTimestamp());
        jobLogPo.setTaskTrackerNodeGroup(bizLogPo.getTaskTrackerNodeGroup());
        jobLogPo.setTaskTrackerIdentity(bizLogPo.getTaskTrackerIdentity());
        jobLogPo.setJobId(bizLogPo.getJobId());
        jobLogPo.setMsg(bizLogPo.getMsg());
        jobLogPo.setLevel(bizLogPo.getLevel());
        jobLogPo.setLogType(LogType.BIZ);
        return jobLogPo;
    }

}
