package com.lts.job.web.support;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.util.StringUtils;
import com.lts.job.queue.*;
import com.lts.job.web.cluster.AdminApplication;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class AdminAppFactoryBean implements FactoryBean<AdminApplication>, InitializingBean {

    CronJobQueueFactory cronJobQueueFactory = ExtensionLoader.getExtensionLoader(
            CronJobQueueFactory.class).getAdaptiveExtension();
    ExecutableJobQueueFactory executableJobQueueFactory = ExtensionLoader.getExtensionLoader(
            ExecutableJobQueueFactory.class).getAdaptiveExtension();
    ExecutingJobQueueFactory executingJobQueueFactory = ExtensionLoader.getExtensionLoader(
            ExecutingJobQueueFactory.class).getAdaptiveExtension();
    NodeGroupStoreFactory nodeGroupStoreFactory = ExtensionLoader.getExtensionLoader(
            NodeGroupStoreFactory.class).getAdaptiveExtension();

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
        node.setIdentity("LTS_admin_" + StringUtils.generateUUID());
        node.addListenNodeType(NodeType.JOB_CLIENT);
        node.addListenNodeType(NodeType.TASK_TRACKER);
        node.addListenNodeType(NodeType.JOB_TRACKER);

        Config config = new Config();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperties("registry.address"));

        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 config. 开头的配置都加入到config中
            if (entry.getKey().startsWith("config.")) {
                config.setParameter(entry.getKey().replace("config.", ""), entry.getValue());
            }
        }

        application = new AdminApplication();
        application.setConfig(config);
        application.setNode(node);
        application.setCronJobQueue(cronJobQueueFactory.getQueue(config));
        application.setExecutableJobQueue(executableJobQueueFactory.getQueue(config));
        application.setExecutingJobQueue(executingJobQueueFactory.getQueue(config));
        application.setNodeGroupStore(nodeGroupStoreFactory.getStore(config));
    }

}
