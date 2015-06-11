package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.commons.file.FileUtils;
import com.lts.job.core.commons.utils.DateUtils;
import com.lts.job.core.support.JobQueueUtils;
import com.lts.job.queue.NodeGroupStore;
import com.lts.job.queue.domain.NodeGroupPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.store.jdbc.JdbcRepository;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class MysqlNodeGroupStore extends JdbcRepository implements NodeGroupStore {

    public MysqlNodeGroupStore(Config config) {
        super(config);
        createStore();
    }

    public void createStore() {
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_node_group_store.sql");
            String sql = FileUtils.read(is);
            sql = sql.replace("{tableName}", JobQueueUtils.NODE_GROUP_STORE);
            getSqlTemplate().update(sql);
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
    }

    private String insertSQL = "INSERT INTO " + JobQueueUtils.NODE_GROUP_STORE
            + " (node_type, name, gmt_created) VALUES (?,?,?)";

    private String removeSQL = "DELETE FROM " + JobQueueUtils.NODE_GROUP_STORE
            + " WHERE node_type = ? and name = ?";

    private String selectSQL = "SELECT * FROM " + JobQueueUtils.NODE_GROUP_STORE
            + " WHERE node_type = ?";

    private String selectOneSQL = "SELECT count(1) FROM " + JobQueueUtils.NODE_GROUP_STORE
            + " WHERE node_type = ? and name = ?";

    private ResultSetHandler<List<NodeGroupPo>> RESULT_SET_HANDLER = new ResultSetHandler<List<NodeGroupPo>>() {
        @Override
        public List<NodeGroupPo> handle(ResultSet rs) throws SQLException {
            List<NodeGroupPo> list = new ArrayList<NodeGroupPo>();
            while (rs.next()) {
                NodeGroupPo nodeGroupPo = new NodeGroupPo();
                nodeGroupPo.setNodeType(NodeType.valueOf(rs.getString("node_type")));
                nodeGroupPo.setName(rs.getString("name"));
                nodeGroupPo.setGmtCreated(rs.getLong("gmt_created"));
                list.add(nodeGroupPo);
            }
            return list;
        }
    };

    @Override
    public void addNodeGroup(NodeType nodeType, String name) {
        try {
            Long count = getSqlTemplate().queryForValue(selectOneSQL, nodeType.name(), name);
            if (count > 0) {
                //  already exist
                return;
            }
            getSqlTemplate().update(insertSQL, nodeType.name(), name, DateUtils.currentTimeMillis());
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public void removeNodeGroup(NodeType nodeType, String name) {
        try {
            getSqlTemplate().update(removeSQL, nodeType, name);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<NodeGroupPo> getNodeGroup(NodeType nodeType) {

        try {
            return getSqlTemplate().query(selectSQL, RESULT_SET_HANDLER, nodeType.name());
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

}
