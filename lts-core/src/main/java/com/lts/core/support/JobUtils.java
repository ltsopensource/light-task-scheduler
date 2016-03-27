package com.lts.core.support;

import com.lts.core.constant.Constants;
import com.lts.queue.domain.JobPo;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class JobUtils {

    public static long getRepeatNextTriggerTime(JobPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(Constants.QUARTZ_FIRST_FIRE_TIME));
        return firstTriggerTime + (jobPo.getRepeatedCount() + 1) * jobPo.getRepeatInterval();
    }

    public static long getRepeatTriggerTime(JobPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(Constants.QUARTZ_FIRST_FIRE_TIME));
        return firstTriggerTime + jobPo.getRepeatedCount() * jobPo.getRepeatInterval();
    }

}
