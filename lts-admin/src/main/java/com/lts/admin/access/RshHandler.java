package com.lts.admin.access;

import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.store.jdbc.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hugui.hg on 3/9/16.
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

}
