package com.github.ltsopensource.monitor;

import com.github.ltsopensource.cmd.HttpCmdServer;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cmd.JVMInfoGetHttpCmd;
import com.github.ltsopensource.core.cmd.StatusCheckHttpCmd;
import com.github.ltsopensource.core.commons.utils.NetUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.compiler.AbstractCompiler;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.factory.JobNodeConfigFactory;
import com.github.ltsopensource.core.factory.NodeFactory;
import com.github.ltsopensource.core.json.JSONFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.registry.AbstractRegistry;
import com.github.ltsopensource.core.registry.Registry;
import com.github.ltsopensource.core.registry.RegistryFactory;
import com.github.ltsopensource.core.registry.RegistryStatMonitor;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.support.AliveKeeping;
import com.github.ltsopensource.ec.EventCenter;
import com.github.ltsopensource.jvmmonitor.JVMMonitor;
import com.github.ltsopensource.monitor.access.MonitorAccessFactory;
import com.github.ltsopensource.monitor.cmd.MDataAddHttpCmd;
import com.github.ltsopensource.monitor.cmd.MDataSrv;

import java.util.concurrent.atomic.AtomicBoolean;

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
    private AtomicBoolean start = new AtomicBoolean(false);

    public MonitorAgent() {
        this.appContext = new MonitorAppContext();
        this.node = NodeFactory.create(MonitorNode.class);
        this.config = JobNodeConfigFactory.getDefaultConfig();
        this.config.setNodeType(node.getNodeType());
        this.appContext.setConfig(config);
    }

    public void start() {

        if (!start.compareAndSet(false, true)) {
            return;
        }

        try {
            // 初始化
            intConfig();

            // 默认端口
            int port = config.getParameter(ExtConfig.HTTP_CMD_PORT, 8730);
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

            LOGGER.info("========== Start Monitor Success");

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

        String compiler = config.getParameter(ExtConfig.COMPILER);
        if (StringUtils.isNotEmpty(compiler)) {
            AbstractCompiler.setCompiler(compiler);
        }
        // 设置json
        String ltsJson = config.getParameter(ExtConfig.LTS_JSON);
        if (StringUtils.isNotEmpty(ltsJson)) {
            JSONFactory.setJSONAdapter(ltsJson);
        }

        if (StringUtils.isEmpty(config.getIp())) {
            config.setIp(NetUtils.getLocalHost());
        }
        JobNodeConfigFactory.buildIdentity(config);

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
    }

    public void stop() {
        if (!start.compareAndSet(true, false)) {
            return;
        }

        try {
            if (registry != null) {
                // 先取消暴露
                this.registry.unregister(node);
            }
            if (httpCmdServer != null) {
                // 停止服务
                this.httpCmdServer.stop();
            }

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
