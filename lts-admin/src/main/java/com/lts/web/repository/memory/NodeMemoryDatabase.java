package com.lts.web.repository.memory;

import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.exception.DaoException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.store.jdbc.SqlBuilder;
import com.lts.web.request.NodeRequest;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@Repository
public class NodeMemoryDatabase extends MemoryDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMemoryDatabase.class);

    private String insertSQL = "INSERT INTO lts_node " +
            "( identity, " +
            "  available, " +
            "  clusterName, " +
            "  nodeType, " +
            "  ip, " +
            "  port, " +
            "  nodeGroup, " +
            "  createTime, " +
            "  threads," +
            "  hostName," +
            "  commandPort)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private String deleteSQL = "DELETE FROM lts_node where identity = ?";

    public NodeMemoryDatabase() {
        createTable();
    }

    private void createTable() {
        String tableSQL = "CREATE TABLE `lts_node` (" +
                "  `identity` varchar(32) NOT NULL DEFAULT ''," +
                "  `available` tinyint(4) DEFAULT NULL," +
                "  `clusterName` varchar(64) DEFAULT NULL," +
                "  `nodeType` varchar(64) DEFAULT NULL," +
                "  `ip` varchar(16) DEFAULT NULL," +
                "  `port` int(11) DEFAULT NULL," +
                "  `nodeGroup` varchar(64) DEFAULT NULL," +
                "  `createTime` bigint(20) DEFAULT NULL," +
                "  `threads` int(11) DEFAULT NULL," +
                "  `hostName` varchar(64) DEFAULT NULL," +
                "  `commandPort` int(11) DEFAULT NULL," +
                "  PRIMARY KEY (`identity`)" +
                ")";

        try {
            getSqlTemplate().update(tableSQL);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void addNode(List<Node> nodes) {
        for (Node node : nodes) {
            try {
                NodeRequest request = new NodeRequest();
                request.setIdentity(node.getIdentity());
                List<Node> existNodes = search(request);
                if (CollectionUtils.isNotEmpty(existNodes)) {
                    // 如果存在,那么先删除
                    removeNode(existNodes);
                }
                getSqlTemplate().update(insertSQL,
                        node.getIdentity(),
                        node.isAvailable() ? 1 : 0,
                        node.getClusterName(),
                        node.getNodeType().name(),
                        node.getIp(),
                        node.getPort(),
                        node.getGroup(),
                        node.getCreateTime(),
                        node.getThreads(),
                        node.getHostName(),
                        node.getCommandPort()
                );
            } catch (Exception e) {
                LOGGER.error("Insert {} error!", node, e);
            }
        }

    }

    public void removeNode(List<Node> nodes) {
        for (Node node : nodes) {
            try {
                getSqlTemplate().update(deleteSQL,
                        node.getIdentity()
                );
            } catch (Exception e) {
                LOGGER.error("Delete {} error!", node, e);
            }
        }
    }

    public List<Node> search(NodeRequest request) {

        try {
            SqlBuilder sql = new SqlBuilder("SELECT * FROM lts_node");
            sql.addCondition("identity", request.getIdentity())
                    .addCondition("nodeGroup", request.getNodeGroup())
                    .addCondition("nodeType", request.getNodeType() == null ? null : request.getNodeType().name())
                    .addCondition("ip", request.getIp())
                    .addCondition("available", request.getAvailable())
                    .addCondition("createTime",
                            request.getStartDate() == null ? null : request.getStartDate().getTime(), ">=")
                    .addCondition("createTime",
                            request.getEndDate() == null ? null : request.getEndDate().getTime(), "<=")
                    .addOrderBy(request.getField(), request.getDirection())
                    .addLimit(request.getStart(), request.getLimit());

            return getSqlTemplate().query(sql.getSQL(), NODE_LIST_RESULT_SET_HANDLER, sql.getParams().toArray());
        } catch (Exception e) {
            LOGGER.error("Search node error!", e);
            throw new DaoException(e);
        }
    }

    private ResultSetHandler<List<Node>> NODE_LIST_RESULT_SET_HANDLER = new ResultSetHandler<List<Node>>() {
        @Override
        public List<Node> handle(ResultSet rs) throws SQLException {
            List<Node> nodes = new ArrayList<Node>();

            while (rs.next()) {
                Node node = new Node();
                node.setIdentity(rs.getString("identity"));
                node.setClusterName(rs.getString("clusterName"));
                node.setNodeType(NodeType.valueOf(rs.getString("nodeType")));
                node.setIp(rs.getString("ip"));
                node.setPort(rs.getInt("port"));
                node.setGroup(rs.getString("nodeGroup"));
                node.setCreateTime(rs.getLong("createTime"));
                node.setThreads(rs.getInt("threads"));
                node.setAvailable(rs.getInt("available") == 1);
                node.setHostName(rs.getString("hostName"));
                node.setCommandPort(rs.getInt("commandPort"));
                nodes.add(node);
            }
            return nodes;
        }
    };
}
