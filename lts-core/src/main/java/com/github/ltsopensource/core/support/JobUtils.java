package com.github.ltsopensource.core.support;

import com.github.ltsopensource.core.commons.utils.BeanUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.support.bean.BeanCopier;
import com.github.ltsopensource.core.support.bean.BeanCopierFactory;
import com.github.ltsopensource.core.support.bean.PropConverter;
import com.github.ltsopensource.queue.domain.JobPo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class JobUtils {

    private static final BeanCopier<Job, Job> JOB_BEAN_COPIER;
    private static final BeanCopier<JobPo, JobPo> JOB_PO_BEAN_COPIER;

    static {
        Map<String, PropConverter<?, ?>> jobPoConverterMap = new ConcurrentHashMap<String, PropConverter<?, ?>>(1);
        // 目前只有这个 extParams和 internalExtParams 不是基本类型, 为了不采用流的方式, 从而提升性能
        jobPoConverterMap.put("extParams", new PropConverter<JobPo, Map<String, String>>() {
            @Override
            public Map<String, String> convert(JobPo jobPo) {
                return BeanUtils.copyMap(jobPo.getExtParams());
            }
        });
        jobPoConverterMap.put("internalExtParams", new PropConverter<JobPo, Map<String, String>>() {
            @Override
            public Map<String, String> convert(JobPo jobPo) {
                return BeanUtils.copyMap(jobPo.getInternalExtParams());
            }
        });
        JOB_PO_BEAN_COPIER = BeanCopierFactory.createCopier(JobPo.class, JobPo.class, jobPoConverterMap);

        Map<String, PropConverter<?, ?>> jobConverterMap = new ConcurrentHashMap<String, PropConverter<?, ?>>(1);
        // 目前只有这个 extParams不是基本类型, 为了不采用流的方式, 从而提升性能
        jobConverterMap.put("extParams", new PropConverter<Job, Map<String, String>>() {
            @Override
            public Map<String, String> convert(Job job) {
                return BeanUtils.copyMap(job.getExtParams());
            }
        });
        JOB_BEAN_COPIER = BeanCopierFactory.createCopier(Job.class, Job.class, jobConverterMap);
    }

    public static long getRepeatNextTriggerTime(JobPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(Constants.FIRST_FIRE_TIME));
        long now = SystemClock.now();
        long remainder = (now - firstTriggerTime) % jobPo.getRepeatInterval();
        if (remainder == 0) {
            return now;
        }
        return now + (jobPo.getRepeatInterval() - remainder);
    }

    public static boolean isRelyOnPrevCycle(JobPo jobPo) {
        return jobPo.getRelyOnPrevCycle() == null || jobPo.getRelyOnPrevCycle();
    }

    public static String generateJobId() {
        return StringUtils.generateUUID();
    }

    public static String generateExeSeqId(JobPo jobPo){
        return String.valueOf(jobPo.getTriggerTime());
    }

    public static Job copy(Job source) {
        Job job = new Job();
        JOB_BEAN_COPIER.copyProps(source, job);
        return job;
    }

    public static JobPo copy(JobPo source) {
        JobPo jobPo = new JobPo();
        JOB_PO_BEAN_COPIER.copyProps(source, jobPo);
        return jobPo;
    }

}
