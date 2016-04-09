package com.lts.monitor;

import com.lts.cmd.HttpCmdServer;
import com.lts.core.cluster.Config;
import com.lts.core.cmd.JVMInfoGetHttpCmd;
import com.lts.core.cmd.StatusCheckHttpCmd;
import com.lts.core.commons.utils.NetUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.factory.JobNodeConfigFactory;
import com.lts.core.factory.NodeFactory;
import com.lts.core.json.JSONFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.registry.AbstractRegistry;
import com.lts.core.registry.Registry;
import com.lts.core.registry.RegistryFactory;
import com.lts.core.registry.RegistryStatMonitor;
import com.lts.core.spi.ServiceLoader;
import com.lts.core.spi.SpiExtensionKey;
import com.lts.core.support.AliveKeeping;
import com.lts.ec.EventCenter;
import com.lts.jvmmonitor.JVMMonitor;
import com.lts.monitor.access.MonitorAccessFactory;
import com.lts.monitor.cmd.MDataAddHttpCmd;
import com.lts.monitor.cmd.MDataSrv;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorAgent.class);
    private HttpCmdServer httpCmdServer;
    private MonitorAppContext appContext;
    private Config config;
    private Registry registry;
    private MonitorNode node;

    public MonitorAgent() {
        this.appContext = new MonitorAppContext();
        this.node = NodeFactory.create(MonitorNode.class);
        this.config = JobNodeConfigFactory.getDefaultConfig();
        this.config.setNodeType(node.getNodeType());
        this.appContext.setConfig(config);
    }

    public void start() {

        try {
            // 初始化
            intConfig();

            // 默认端口
            int port = config.getParameter("lts.http.cmd.port", 8730);
            this.httpCmdServer = HttpCmdServer.Factory.getHttpCmdServer(config.getIp(), port);

            this.httpCmdServer.registerCommands(
                    new MDataAddHttpCmd(this.appContext),
                    new StatusCheckHttpCmd(config),
                    new JVMInfoGetHttpCmd(config));
            // 启动
            this.httpCmdServer.start();

            // 设置真正启动的端口
            this.appContext.setHttpCmdPort(httpCmdServer.getPort());

            initNode();

            // 暴露在 zk 上
            initRegistry();
            registry.register(node);

            JVMMonitor.start();
            AliveKeeping.start();

            LOGGER.error("========== Start Monitor Success");

        } catch (Throwable t) {
            LOGGER.error("========== Start Monitor Error:", t);
        }
    }

    public void initRegistry() {
        registry = RegistryFactory.getRegistry(appContext);
        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(node);
        }
    }

    private void initNode() {
        config.setListenPort(this.appContext.getHttpCmdPort());
        NodeFactory.build(node, config);
        this.node.setHttpCmdPort(this.appContext.getHttpCmdPort());
    }

    private void intConfig() {
        // 初始化一些 db access
        MonitorAccessFactory factory = ServiceLoader.load(MonitorAccessFactory.class, config);
        this.appContext.setJobTrackerMAccess(factory.getJobTrackerMAccess(config));
        this.appContext.setJvmGCAccess(factory.getJVMGCAccess(config));
        this.appContext.setJvmMemoryAccess(factory.getJVMMemoryAccess(config));
        this.appContext.setJvmThreadAccess(factory.getJVMThreadAccess(config));
        this.appContext.setTaskTrackerMAccess(factory.getTaskTrackerMAccess(config));
        this.appContext.setJobClientMAccess(factory.getJobClientMAccess(config));

        this.appContext.setMDataSrv(new MDataSrv(this.appContext));

        this.appContext.setEventCenter(ServiceLoader.load(EventCenter.class, config));
        this.appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));

        // 设置json
        String ltsJson = config.getParameter(SpiExtensionKey.LTS_JSON);
        if (StringUtils.isNotEmpty(ltsJson)) {
            JSONFactory.setJSONAdapter(ltsJson);
        }
        if (StringUtils.isEmpty(config.getIp())) {
            config.setIp(NetUtils.getLocalHost());
        }
        JobNodeConfigFactory.buildIdentity(config);
    }

    public void stop() {
        try {
            // 先取消暴露
            this.registry.unregister(node);
            // 停止服务
            this.httpCmdServer.stop();

            JVMMonitor.stop();
            AliveKeeping.stop();

            LOGGER.error("========== Stop Monitor Success");

        } catch (Throwable t) {
            LOGGER.error("========== Stop Monitor Error:", t);
        }
    }

    /**
     * 设置集群名字
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 设置zookeeper注册中心地址
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * 设置额外的配置参数
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }

    /**
     * 节点标识(必须要保证这个标识是唯一的才能设置，请谨慎设置)
     * 这个是非必须设置的，建议使用系统默认生成
     */
    public void setIdentity(String identity) {
        config.setIdentity(identity);
    }

    /**
     * 显示设置绑定ip
     */
    public void setBindIp(String bindIp) {
        if (StringUtils.isEmpty(bindIp)
                || !NetUtils.isValidHost(bindIp)
                ) {
            throw new IllegalArgumentException("Invalided bind ip:" + bindIp);
        }
        config.setIp(bindIp);
    }
}
