package com.lts.admin.access;

import com.lts.admin.access.domain.NodeOnOfflineLog;
import com.lts.admin.web.vo.NodeInfo;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.monitor.access.domain.JobClientMDataPo;
import com.lts.monitor.access.domain.JobTrackerMDataPo;
import com.lts.monitor.access.domain.TaskTrackerMDataPo;
import com.lts.store.jdbc.dbutils.JdbcTypeUtils;
import com.lts.store.jdbc.dbutils.ResultSetHandler;

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
                nodes.add(node);
            }
            return nodes;
        }
    };

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
                log.setLogTime(JdbcTypeUtils.toDate(rs.getLong("log_time")));
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
//                po.setId(rs.getString("id"));
//                po.setGmtCreated(rs.getLong("gmt_created"));
                po.setTimestamp(rs.getLong("timestamp"));
//                po.setNodeType(NodeType.convert(rs.getString("node_type")));
//                po.setNodeGroup(rs.getString("node_group"));
//                po.setIdentity(rs.getString("identity"));

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
//                po.setId(rs.getString("id"));
//                po.setGmtCreated(rs.getLong("gmt_created"));
                po.setTimestamp(rs.getLong("timestamp"));
//                po.setNodeType(NodeType.convert(rs.getString("node_type")));
//                po.setNodeGroup(rs.getString("node_group"));
//                po.setIdentity(rs.getString("identity"));

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
}
