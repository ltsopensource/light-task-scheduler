package com.github.ltsopensource.admin.access;

import com.github.ltsopensource.admin.access.domain.NodeOnOfflineLog;
import com.github.ltsopensource.admin.web.vo.NodeInfo;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.monitor.access.domain.*;
import com.github.ltsopensource.store.jdbc.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class RshHandler {

    public static final ResultSetHandler<List<Node>> NODE_LIST_RSH = new ResultSetHandler<List<Node>>() {
        @Override
        public List<Node> handle(ResultSet rs) throws SQLException {
            List<Node> nodes = new ArrayList<Node>();

            while (rs.next()) {
                nodes.add(getNodeByRs(rs));
            }
            return nodes;
        }
    };

    public static final ResultSetHandler<Node> NODE_RSH = new ResultSetHandler<Node>() {
        @Override
        public Node handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                return getNodeByRs(rs);
            }
            return null;
        }
    };

    private static Node getNodeByRs(final ResultSet rs) throws SQLException {
        Node node = new Node();
        node.setIdentity(rs.getString("identity"));
        node.setClusterName(rs.getString("cluster_name"));
        node.setNodeType(NodeType.valueOf(rs.getString("node_type")));
        node.setIp(rs.getString("ip"));
        node.setPort(rs.getInt("port"));
        node.setGroup(rs.getString("node_group"));
        node.setCreateTime(rs.getLong("create_time"));
        node.setThreads(rs.getInt("threads"));
        node.setAvailable(rs.getInt("available") == 1);
        node.setHostName(rs.getString("host_name"));
        node.setHttpCmdPort(rs.getInt("http_cmd_port"));
        return node;
    }

    public static final ResultSetHandler<List<JobTrackerMDataPo>> JOB_TRACKER_SUM_M_DATA_RSH = new ResultSetHandler<List<JobTrackerMDataPo>>() {
        @Override
        public List<JobTrackerMDataPo> handle(ResultSet rs) throws SQLException {

            List<JobTrackerMDataPo> list = new ArrayList<JobTrackerMDataPo>();

            while (rs.next()) {
                JobTrackerMDataPo po = new JobTrackerMDataPo();
//                po.setId(rs.getString("id"));
//                po.setGmtCreated(rs.getLong("gmt_created"));
                po.setTimestamp(rs.getLong("timestamp"));
//                po.setNodeType(NodeType.convert(rs.getString("node_type")));
//                po.setNodeGroup(rs.getString("node_group"));
//                po.setIdentity(rs.getString("identity"));
                po.setReceiveJobNum(rs.getLong("receive_job_num"));
                po.setPushJobNum(rs.getLong("push_job_num"));
                po.setExeSuccessNum(rs.getLong("exe_success_num"));
                po.setExeFailedNum(rs.getLong("exe_failed_num"));
                po.setExeLaterNum(rs.getLong("exe_later_num"));
                po.setExeExceptionNum(rs.getLong("exe_exception_num"));
                po.setFixExecutingJobNum(rs.getLong("fix_executing_job_num"));
                list.add(po);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<NodeOnOfflineLog>> NODE_ON_OFFLINE_LOG_LIST_RSH = new ResultSetHandler<List<NodeOnOfflineLog>>() {
        @Override
        public List<NodeOnOfflineLog> handle(ResultSet rs) throws SQLException {

            List<NodeOnOfflineLog> list = new ArrayList<NodeOnOfflineLog>();
            while (rs.next()) {
                NodeOnOfflineLog log = new NodeOnOfflineLog();
                log.setLogTime(rs.getTimestamp("log_time"));
                log.setEvent(rs.getString("event"));
                log.setNodeType(NodeType.convert(rs.getString("node_type")));
                log.setClusterName(rs.getString("cluster_name"));
                log.setIp(rs.getString("ip"));
                log.setPort(rs.getInt("port"));
                log.setHostName(rs.getString("host_name"));
                log.setGroup(rs.getString("group"));
                log.setCreateTime(rs.getLong("create_time"));
                log.setThreads(rs.getInt("threads"));
                log.setIdentity(rs.getString("identity"));
                log.setHttpCmdPort(rs.getInt("http_cmd_port"));
                list.add(log);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<TaskTrackerMDataPo>> TASK_TRACKER_SUM_M_DATA_RSH = new ResultSetHandler<List<TaskTrackerMDataPo>>() {
        @Override
        public List<TaskTrackerMDataPo> handle(ResultSet rs) throws SQLException {
            List<TaskTrackerMDataPo> list = new ArrayList<TaskTrackerMDataPo>();

            while (rs.next()) {
                TaskTrackerMDataPo po = new TaskTrackerMDataPo();
                po.setTimestamp(rs.getLong("timestamp"));

                po.setExeSuccessNum(rs.getLong("exe_success_num"));
                po.setExeFailedNum(rs.getLong("exe_failed_num"));
                po.setExeLaterNum(rs.getLong("exe_later_num"));
                po.setExeExceptionNum(rs.getLong("exe_exception_num"));
                po.setTotalRunningTime(rs.getLong("total_running_time"));

                list.add(po);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<JobClientMDataPo>> JOB_CLIENT_SUM_M_DATA_RSH = new ResultSetHandler<List<JobClientMDataPo>>() {
        @Override
        public List<JobClientMDataPo> handle(ResultSet rs) throws SQLException {
            List<JobClientMDataPo> list = new ArrayList<JobClientMDataPo>();

            while (rs.next()) {
                JobClientMDataPo po = new JobClientMDataPo();
                po.setTimestamp(rs.getLong("timestamp"));

                po.setSubmitSuccessNum(rs.getLong("submit_success_num"));
                po.setSubmitFailedNum(rs.getLong("submit_failed_num"));
                po.setFailStoreNum(rs.getLong("fail_store_num"));
                po.setSubmitFailStoreNum(rs.getLong("submit_fail_store_num"));
                po.setHandleFeedbackNum(rs.getLong("handle_feedback_num"));
                list.add(po);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<NodeInfo>> NODE_INFO_LIST_RSH = new ResultSetHandler<List<NodeInfo>>() {
        @Override
        public List<NodeInfo> handle(ResultSet rs) throws SQLException {
            List<NodeInfo> list = new ArrayList<NodeInfo>();

            while (rs.next()) {
                NodeInfo nodeInfo = new NodeInfo();
                nodeInfo.setIdentity(rs.getString("identity"));
                nodeInfo.setNodeGroup(rs.getString("node_group"));
                list.add(nodeInfo);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<JVMMemoryDataPo>> JVM_MEMORY_SUM_M_DATA_RSH = new ResultSetHandler<List<JVMMemoryDataPo>>() {
        @Override
        public List<JVMMemoryDataPo> handle(ResultSet rs) throws SQLException {
            List<JVMMemoryDataPo> list = new ArrayList<JVMMemoryDataPo>();

            while (rs.next()) {
                JVMMemoryDataPo po = new JVMMemoryDataPo();
                po.setTimestamp(rs.getLong("timestamp"));

                po.setHeapMemoryCommitted(rs.getLong("heap_memory_committed"));
                po.setHeapMemoryInit(rs.getLong("heap_memory_init"));
                po.setHeapMemoryMax(rs.getLong("heap_memory_max"));
                po.setHeapMemoryUsed(rs.getLong("heap_memory_used"));
                po.setNonHeapMemoryCommitted(rs.getLong("non_heap_memory_committed"));
                po.setNonHeapMemoryInit(rs.getLong("non_heap_memory_init"));
                po.setNonHeapMemoryMax(rs.getLong("non_heap_memory_max"));
                po.setNonHeapMemoryUsed(rs.getLong("non_heap_memory_used"));
                po.setPermGenCommitted(rs.getLong("perm_gen_committed"));
                po.setPermGenInit(rs.getLong("perm_gen_init"));
                po.setPermGenMax(rs.getLong("perm_gen_max"));
                po.setPermGenUsed(rs.getLong("perm_gen_used"));
                po.setOldGenCommitted(rs.getLong("old_gen_committed"));
                po.setOldGenInit(rs.getLong("old_gen_init"));
                po.setOldGenMax(rs.getLong("old_gen_max"));
                po.setOldGenUsed(rs.getLong("old_gen_used"));
                po.setEdenSpaceCommitted(rs.getLong("eden_space_committed"));
                po.setEdenSpaceInit(rs.getLong("eden_space_init"));
                po.setEdenSpaceMax(rs.getLong("eden_space_max"));
                po.setEdenSpaceUsed(rs.getLong("eden_space_used"));
                po.setSurvivorCommitted(rs.getLong("survivor_committed"));
                po.setSurvivorInit(rs.getLong("survivor_init"));
                po.setSurvivorMax(rs.getLong("survivor_max"));
                po.setSurvivorUsed(rs.getLong("survivor_used"));

                list.add(po);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<JVMGCDataPo>> JVM_GC_SUM_M_DATA_RSH = new ResultSetHandler<List<JVMGCDataPo>>() {
        @Override
        public List<JVMGCDataPo> handle(ResultSet rs) throws SQLException {
            List<JVMGCDataPo> list = new ArrayList<JVMGCDataPo>();

            while (rs.next()) {
                JVMGCDataPo po = new JVMGCDataPo();
                po.setTimestamp(rs.getLong("timestamp"));

                po.setYoungGCCollectionCount(rs.getLong("young_gc_collection_count"));
                po.setYoungGCCollectionTime(rs.getLong("young_gc_collection_time"));
                po.setFullGCCollectionCount(rs.getLong("full_gc_collection_count"));
                po.setFullGCCollectionTime(rs.getLong("full_gc_collection_time"));
                po.setSpanYoungGCCollectionCount(rs.getLong("span_young_gc_collection_count"));
                po.setSpanYoungGCCollectionTime(rs.getLong("span_young_gc_collection_time"));
                po.setSpanFullGCCollectionCount(rs.getLong("span_full_gc_collection_count"));
                po.setSpanFullGCCollectionTime(rs.getLong("span_full_gc_collection_time"));

                list.add(po);
            }
            return list;
        }
    };

    public static final ResultSetHandler<List<JVMThreadDataPo>> JVM_THREAD_SUM_M_DATA_RSH = new ResultSetHandler<List<JVMThreadDataPo>>() {
        @Override
        public List<JVMThreadDataPo> handle(ResultSet rs) throws SQLException {
            List<JVMThreadDataPo> list = new ArrayList<JVMThreadDataPo>();

            while (rs.next()) {
                JVMThreadDataPo po = new JVMThreadDataPo();
                po.setTimestamp(rs.getLong("timestamp"));

                po.setDaemonThreadCount(rs.getInt("daemon_thread_count"));
                po.setThreadCount(rs.getInt("thread_count"));
                po.setTotalStartedThreadCount(rs.getLong("total_started_thread_count"));
                po.setDeadLockedThreadCount(rs.getInt("dead_locked_thread_count"));
                po.setProcessCpuTimeRate(rs.getDouble("process_cpu_time_rate"));
                list.add(po);
            }
            return list;
        }
    };
}
