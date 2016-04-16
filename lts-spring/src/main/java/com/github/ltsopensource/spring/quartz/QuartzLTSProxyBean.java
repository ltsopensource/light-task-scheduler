package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
public class QuartzLTSProxyBean implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzLTSProxyBean.class);
    // 是否使用LTS
    private boolean ltsEnable = true;

    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 节点组名称
     */
    private String nodeGroup;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 提交失败任务存储路径 , 默认用户目录
     */
    private String dataPath;
    /**
     * 如果为true, 每次启动时, 则以本地为准, 覆盖lts上的
     * 如果为false,每次启动是, 则以lts为准, 如果lts上已经存在, 则不添加
     */
    private boolean replaceOnExist = true;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (ltsEnable) {
            // 如果启用才进行代理
            LOGGER.info("========LTS====== Proxy Quartz Scheduler");

            // 参数check TODO
            QuartzLTSConfig quartzLTSConfig = new QuartzLTSConfig();
            quartzLTSConfig.setClusterName(clusterName);
            quartzLTSConfig.setNodeGroup(nodeGroup);
            quartzLTSConfig.setRegistryAddress(registryAddress);
            quartzLTSConfig.setDataPath(dataPath);
            quartzLTSConfig.setReplaceOnExist(replaceOnExist);

            QuartzLTSProxyAgent agent = new QuartzLTSProxyAgent(quartzLTSConfig);
            QuartzProxyContext context = new QuartzProxyContext(quartzLTSConfig, agent);

            QuartzSchedulerBeanRegistrar registrar = new QuartzSchedulerBeanRegistrar(context);
            beanFactory.addPropertyEditorRegistrar(registrar);
        }
    }

    public void setLtsEnable(boolean ltsEnable) {
        this.ltsEnable = ltsEnable;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setReplaceOnExist(boolean replaceOnExist) {
        this.replaceOnExist = replaceOnExist;
    }
}
