package com.lts.job.web.support.db;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.StringUtils;
import com.lts.job.web.request.NodeRequest;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class NodeMemDB extends MemDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMemDB.class);

    private String insertSQL = "INSERT INTO lts_node " +
            "( identity, " +
            "  available, " +
            "  clusterName, " +
            "  nodeType, " +
            "  ip, " +
            "  port, " +
            "  nodeGroup, " +
            "  createTime, " +
            "  threads)" +
            " VALUES (?,?,?,?,?,?,?,?,?)";

    private String deleteSQL = "DELETE FROM lts_node where identity = ?";

    public NodeMemDB() {
        createTable();
    }

    private void createTable() {
        String tableSQL = "CREATE TABLE lts_node (" +
                "  identity varchar(32) NOT NULL," +
                "  available tinyint ," +
                "  clusterName varchar(64) ," +
                "  nodeType varchar(16) ," +
                "  ip varchar(16) ," +
                "  port int ," +
                "  nodeGroup varchar(64) ," +
                "  createTime bigint ," +
                "  threads int ," +
                "  PRIMARY KEY (identity))";

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
                        node.getThreads()
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
            StringBuilder conditionSQL = new StringBuilder();

            List<Object> params = new ArrayList<Object>();
            Boolean hasCondition = false;
            if (StringUtils.isNotEmpty(request.getIdentity())) {
                conditionSQL.append(" identity = ? AND ");
                params.add(request.getIdentity());
                hasCondition = true;
            }
            if (StringUtils.isNotEmpty(request.getClusterName())) {
                conditionSQL.append(" clusterName = ? AND ");
                params.add(request.getClusterName());
                hasCondition = true;
            }
            if (StringUtils.isNotEmpty(request.getNodeGroup())) {
                conditionSQL.append(" nodeGroup = ? AND ");
                params.add(request.getNodeGroup());
                hasCondition = true;
            }
            if(request.getNodeType() != null){
                conditionSQL.append(" nodeType = ? AND ");
                params.add(request.getNodeType().name());
                hasCondition = true;
            }
            if (StringUtils.isNotEmpty(request.getIp())) {
                conditionSQL.append(" ip = ? AND ");
                params.add(request.getIp());
                hasCondition = true;
            }
            if (request.getAvailable() != null) {
                conditionSQL.append(" available = ? AND ");
                params.add(request.getAvailable());
                hasCondition = true;
            }
            if (request.getStartDate() != null) {
                conditionSQL.append(" createTime >= ? AND ");
                params.add(request.getStartDate().getTime());
                hasCondition = true;
            }
            if (request.getEndDate() != null) {
                conditionSQL.append(" createTime <= ? AND ");
                params.add(request.getEndDate().getTime());
                hasCondition = true;
            }

            StringBuilder selectSQL = new StringBuilder("SELECT * FROM lts_node ");
            if (hasCondition) {
                conditionSQL.delete(conditionSQL.lastIndexOf("AND"), conditionSQL.length());
                selectSQL.append(" WHERE ").append(conditionSQL);
            }

            if (request.getStart() != null && request.getLimit() != null) {
                selectSQL.append(" LIMIT ").append(request.getStart()).append(",").append(request.getLimit());
            }

            return getSqlTemplate().query(selectSQL.toString(),
                    new ResultSetHandler<List<Node>>() {
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
                                node.setAvailable( rs.getInt("available") == 1? true:false);
                                nodes.add(node);
                            }
                            return nodes;
                        }
                    },
                    params.toArray()
            );
        } catch (Exception e) {
            LOGGER.error("Search node error!", e);
            throw new RuntimeException(e);
        }
    }
}
