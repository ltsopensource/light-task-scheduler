package com.lts.web.support.spring;

import com.lts.biz.logger.JobLoggerDelegate;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.NetUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.registry.RegistryStatMonitor;
import com.lts.core.spi.ServiceLoader;
import com.lts.core.support.SystemClock;
import com.lts.ec.EventCenter;
import com.lts.queue.*;
import com.lts.web.cluster.AdminApplication;
import com.lts.web.support.AppConfigurer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class AdminAppFactoryBean implements FactoryBean<AdminApplication>, InitializingBean {

    private AdminApplication application;

    @Override
    public AdminApplication getObject() throws Exception {
        return application;
    }

    @Override
    public Class<?> getObjectType() {
        return AdminApplication.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Node node = new Node();
        node.setCreateTime(SystemClock.now());
        node.setIp(NetUtils.getLocalHost());
        node.setHostName(NetUtils.getLocalHostName());
        node.setIdentity(Constants.ADMIN_ID_PREFIX + StringUtils.generateUUID());
        node.addListenNodeType(NodeType.JOB_CLIENT);
        node.addListenNodeType(NodeType.TASK_TRACKER);
        node.addListenNodeType(NodeType.JOB_TRACKER);

        Config config = new Config();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperty("registryAddress"));
        String clusterName = AppConfigurer.getProperty("clusterName");
        if (StringUtils.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName in lts-admin.cfg can not be null.");
        }
        config.setClusterName(clusterName);

        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 config. 开头的配置都加入到config中
            if (entry.getKey().startsWith("configs.")) {
                config.setParameter(entry.getKey().replaceFirst("configs.", ""), entry.getValue());
            }
        }

        application = new AdminApplication();
        application.setConfig(config);
        application.setNode(node);
        application.setJobFeedbackQueue(ServiceLoader.load(JobFeedbackQueueFactory.class, config).getQueue(config));
        application.setCronJobQueue(ServiceLoader.load(CronJobQueueFactory.class, config).getQueue(config));
        application.setExecutableJobQueue(ServiceLoader.load(ExecutableJobQueueFactory.class, config).getQueue(config));
        application.setExecutingJobQueue(ServiceLoader.load(ExecutingJobQueueFactory.class, config).getQueue(config));
        application.setNodeGroupStore(ServiceLoader.load(NodeGroupStoreFactory.class, config).getStore(config));
        application.setJobLogger(new JobLoggerDelegate(config));
        application.setEventCenter(ServiceLoader.load(EventCenter.class, config));
        application.setRegistryStatMonitor(new RegistryStatMonitor(application));
    }

}
