package com.lts.web.support;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.domain.Response;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Robert HG (254963746@qq.com) on 10/3/15.
 */
@Component
public class LtsAdminJobClient implements InitializingBean {

    private JobClient jobClient;

    public Response submitJob(Job job) {
        return jobClient.submitJob(job);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jobClient = new JobClient();
        jobClient.setNodeGroup("LTS-Admin");
        String clusterName = AppConfigurer.getProperties("clusterName");
        if (StringUtils.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName in lts-admin.cfg can not be null.");
        }
        jobClient.setClusterName(clusterName);
        jobClient.setRegistryAddress(AppConfigurer.getProperties("registryAddress"));

        jobClient.start();
    }

}
