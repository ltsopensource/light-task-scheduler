package com.lts.admin.support;

import com.lts.admin.access.BackendAccessFactory;
import com.lts.admin.access.memory.NodeMemCacheAccess;
import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.cluster.BackendNode;
import com.lts.admin.cluster.BackendRegistrySrv;
import com.lts.admin.web.support.NoRelyJobGenerator;
import com.lts.biz.logger.SmartJobLogger;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.BeanUtils;
import com.lts.core.commons.utils.NetUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.registry.RegistryStatMonitor;
import com.lts.core.spi.ServiceLoader;
import com.lts.core.support.SystemClock;
import com.lts.ec.EventCenter;
import com.lts.queue.JobQueueFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class BackendAppContextFactoryBean implements FactoryBean<BackendAppContext>, InitializingBean {

    private BackendAppContext appContext;

    @Override
    public BackendAppContext getObject() throws Exception {
        return appContext;
    }

    @Override
    public Class<?> getObjectType() {
        return BackendAppContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Node node = new BackendNode();
        node.setCreateTime(SystemClock.now());
        node.setIp(NetUtils.getLocalHost());
        node.setHostName(NetUtils.getLocalHostName());
        node.setIdentity(Constants.ADMIN_ID_PREFIX + StringUtils.generateUUID());

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

        appContext = new BackendAppContext();
        appContext.setConfig(config);
        appContext.setNode(node);
        appContext.setEventCenter(ServiceLoader.load(EventCenter.class, config));
        appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));
        appContext.setBackendRegistrySrv(new BackendRegistrySrv(appContext));

        initAccess(config);

        // ----------------------下面是JobQueue的配置---------------------------
        Config jobTConfig = (Config) BeanUtils.deepClone(config);
        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 jobT. 开头的配置都加入到jobTConfig中
            if (entry.getKey().startsWith("jobT.")) {
                String key = entry.getKey().replace("jobT.", "");
                String value = entry.getValue();
                jobTConfig.setParameter(key, value);
            }
        }
        initJobQueue(jobTConfig);

        appContext.getBackendRegistrySrv().start();
    }

    private void initJobQueue(Config config) {
        JobQueueFactory factory = ServiceLoader.load(JobQueueFactory.class, config);
        appContext.setExecutableJobQueue(factory.getExecutableJobQueue(config));
        appContext.setExecutingJobQueue(factory.getExecutingJobQueue(config));
        appContext.setCronJobQueue(factory.getCronJobQueue(config));
        appContext.setRepeatJobQueue(factory.getRepeatJobQueue(config));
        appContext.setSuspendJobQueue(factory.getSuspendJobQueue(config));
        appContext.setJobFeedbackQueue(factory.getJobFeedbackQueue(config));
        appContext.setNodeGroupStore(factory.getNodeGroupStore(config));
        appContext.setJobLogger(new SmartJobLogger(appContext));
        appContext.setNoRelyJobGenerator(new NoRelyJobGenerator(appContext));
    }

    private void initAccess(Config config) {
        BackendAccessFactory factory = ServiceLoader.load(BackendAccessFactory.class, config);
        appContext.setBackendJobClientMAccess(factory.getBackendJobClientMAccess(config));
        appContext.setBackendJobTrackerMAccess(factory.getJobTrackerMAccess(config));
        appContext.setBackendTaskTrackerMAccess(factory.getBackendTaskTrackerMAccess(config));
        appContext.setBackendJVMGCAccess(factory.getBackendJVMGCAccess(config));
        appContext.setBackendJVMMemoryAccess(factory.getBackendJVMMemoryAccess(config));
        appContext.setBackendJVMThreadAccess(factory.getBackendJVMThreadAccess(config));
        appContext.setBackendNodeOnOfflineLogAccess(factory.getBackendNodeOnOfflineLogAccess(config));
        appContext.setNodeMemCacheAccess(new NodeMemCacheAccess());
    }

}
