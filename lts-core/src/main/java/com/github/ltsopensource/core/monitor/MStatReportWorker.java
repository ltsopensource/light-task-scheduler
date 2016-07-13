package com.github.ltsopensource.core.monitor;

import com.github.ltsopensource.cmd.DefaultHttpCmd;
import com.github.ltsopensource.cmd.HttpCmd;
import com.github.ltsopensource.cmd.HttpCmdClient;
import com.github.ltsopensource.cmd.HttpCmdResponse;
import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.cmd.HttpCmdNames;
import com.github.ltsopensource.core.cmd.HttpCmdParamNames;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.domain.monitor.MData;
import com.github.ltsopensource.core.domain.monitor.MNode;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.loadbalance.LoadBalance;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jvmmonitor.JVMCollector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Robert HG (254963746@qq.com) on 3/11/16.
 */
public class MStatReportWorker implements Runnable {

    protected final Logger LOGGER = LoggerFactory.getLogger(MStatReportWorker.class);

    private int interval = 1;    // 1分钟
    private Integer preMinute = null;  // 上一分钟
    private AppContext appContext;
    private AbstractMStatReporter reporter;
    // 这里面保存发送失败的，不过有个最大限制，防止内存爆掉

    private PriorityBlockingQueue<MData> queue = new PriorityBlockingQueue<MData>(16, new Comparator<MData>() {
        @Override
        public int compare(MData o1, MData o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    });
    private final static int MAX_RETRY_RETAIN = 500;
    private final static int BATCH_REPORT_SIZE = 10;
    private volatile boolean running = false;
    private LoadBalance loadBalance;

    public MStatReportWorker(AppContext appContext, AbstractMStatReporter reporter) {
        this.appContext = appContext;
        this.reporter = reporter;
        interval = appContext.getConfig().getParameter(ExtConfig.LTS_MONITOR_REPORT_INTERVAL, 1);
        this.loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig(), ExtConfig.MONITOR_SELECT_LOADBALANCE);
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;

        try {
            Calendar calendar = Calendar.getInstance();
            int minute = calendar.get(Calendar.MINUTE);
            if (preMinute == null) {
                preMinute = minute;
                return;
            }

            int diff = minute - preMinute;
            diff = diff < 0 ? diff + 60 : diff;
            if (diff != 0 && diff % interval == 0) {
                try {
                    // 变化超过了间隔时间，要立马收集
                    MData mData = reporter.collectMData();
                    long seconds = SystemClock.now() / 1000;
                    seconds = seconds - (seconds % 60);        // 所有都向下取整，保证是60的倍数
                    seconds = seconds - interval * 60;        // 算其实时间点的数据
                    mData.setTimestamp(seconds * 1000);
                    // JVM monitor
                    mData.setJvmMData(JVMCollector.collect());
                    // report
                    report(mData);

                } finally {
                    preMinute = minute;
                }
            }

        } catch (Throwable t) {
            LOGGER.error("MStatReportWorker collect failed.", t);
        } finally {
            running = false;
        }
    }

    private void report(MData mData) {

        int size = queue.size();

        if (size >= MAX_RETRY_RETAIN) {
            int needRemoveSize = size - (MAX_RETRY_RETAIN - 1);
            for (int i = 0; i < needRemoveSize; i++) {
                queue.poll();
            }
        }
        queue.add(mData);

        final List<Node> monitorNodes = appContext.getSubscribedNodeManager().getNodeList(NodeType.MONITOR);
        if (CollectionUtils.isEmpty(monitorNodes)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Please Start LTS-Monitor");
            }
            return;
        }

        while (queue.size() > 0) {
            List<MData> list = new ArrayList<MData>();
            queue.drainTo(list, BATCH_REPORT_SIZE);

            boolean success = false;
            try {
                HttpCmd cmd = new DefaultHttpCmd();
                cmd.setCommand(HttpCmdNames.HTTP_CMD_ADD_M_DATA);
                cmd.addParam(HttpCmdParamNames.M_NODE, JSON.toJSONString(buildMNode()));
                cmd.addParam(HttpCmdParamNames.M_DATA, JSON.toJSONString(list));

                if (sendReq(monitorNodes, cmd)) {
                    success = true;
                }
            } catch (Throwable t) {
                LOGGER.warn("Report monitor data Error : " + t.getMessage(), t);
            } finally {
                if (!success) {
                    // 放回去
                    queue.addAll(list);
                }
            }
            if (!success) {
                // 停止while
                break;
            }
        }
    }

    // 发送请求
    private boolean sendReq(List<Node> monitorNodes, HttpCmd cmd) {
        while (true) {
            Node node = selectMNode(monitorNodes);
            try {
                cmd.setNodeIdentity(node.getIdentity());
                HttpCmdResponse response = HttpCmdClient.doPost(node.getIp(), node.getPort(), cmd);
                if (response.isSuccess()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Report Monitor Data Success.");
                    }
                    return true;
                } else {
                    LOGGER.warn("Report Monitor Data Failed: " + response.getMsg());
                    monitorNodes.remove(node);
                }
            } catch (Exception e) {
                LOGGER.warn("Report Monitor Data Error: " + e.getMessage(), e);
                // 重试下一个
            }
            if (monitorNodes.size() == 0) {
                return false;
            }
        }
    }

    private Node selectMNode(List<Node> monitorNodes) {
        return loadBalance.select(monitorNodes, appContext.getConfig().getIdentity());
    }

    private MNode buildMNode() {
        MNode mNode = new MNode();
        mNode.setNodeType(reporter.getNodeType());
        mNode.setNodeGroup(appContext.getConfig().getNodeGroup());
        mNode.setIdentity(appContext.getConfig().getIdentity());
        return mNode;
    }
}
