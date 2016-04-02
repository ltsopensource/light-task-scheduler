package com.lts.core.support.bean;

import com.lts.core.domain.Job;
import com.lts.queue.domain.JobPo;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class BeanCopierFactoryTest {

    JobPo jobPo;

    @Before
    public void init() {
        jobPo = new JobPo();
        jobPo.setJobId("dsfdasf");
        jobPo.setTaskId("dfasfs");
        jobPo.setPriority(5);
        jobPo.setInternalExtParam("xxx", "fadsfsa");
    }

    @Test
    public void testGetBeanCopier() throws Exception {
        BeanCopier<JobPo, JobPo> beanCopier = BeanCopierFactory.getBeanCopier(JobPo.class, JobPo.class);

        JobPo jobPo2 = new JobPo();
        beanCopier.copyProps(jobPo, jobPo2);
        System.out.println(jobPo2);

        Job job = new Job();
        BeanCopier<JobPo, Job> beanCopier2 = BeanCopierFactory.getBeanCopier(JobPo.class, Job.class);
        beanCopier2.copyProps(jobPo, job);
        System.out.println(job);
    }

    @Test
    public void testPropConvert() {
        Map<String, PropConverter<?, ?>> map = new HashMap<String, PropConverter<?, ?>>();
        map.put("taskId", new MyPropConverter());
        BeanCopier<JobPo, Job> beanCopier = BeanCopierFactory.getBeanCopier(JobPo.class, Job.class, map);
        Job job = new Job();
        beanCopier.copyProps(jobPo, job);
        System.out.println(job);
    }

    public class MyPropConverter implements PropConverter<JobPo, String> {
        @Override
        public String convert(JobPo jobPo) {
            return "测试的发生的发生";
        }
    }
}