package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.cluster.NodeType;
import com.lts.core.domain.NodeGroupGetRequest;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.NodeGroupStore;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.queue.mysql.support.RshHolder;
import com.lts.store.jdbc.JdbcAbstractAccess;
import com.lts.store.jdbc.builder.*;
import com.lts.admin.response.PaginationRsp;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class MysqlNodeGroupStore extends JdbcAbstractAccess implements NodeGroupStore {

    public MysqlNodeGroupStore(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_node_group_store.sql", JobQueueUtils.NODE_GROUP_STORE));
    }

    @Override
    public void addNodeGroup(NodeType nodeType, String name) {

        Long count = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .where("node_type = ?", nodeType.name())
                .and("name = ?", name)
                .single();
        if (count > 0) {
            //  already exist
            return;
        }
        new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("node_type", "name", "gmt_created")
                .values(nodeType.name(), name, SystemClock.now())
                .doInsert();
    }

    @Override
    public void removeNodeGroup(NodeType nodeType, String name) {
        new DeleteSql(getSqlTemplate())
                .delete(getTableName())
                .where("node_type = ?", nodeType.name())
                .and("name = ?", name)
                .doDelete();
    }

    @Override
    public List<NodeGroupPo> getNodeGroup(NodeType nodeType) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("node_type = ?", nodeType.name())
                .list(RshHolder.NODE_GROUP_LIST_RSH);
    }

    public PaginationRsp<NodeGroupPo> getNodeGroup(NodeGroupGetRequest request) {
        PaginationRsp<NodeGroupPo> response = new PaginationRsp<NodeGroupPo>();

        Long results = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(
                        new WhereSql()
                        .andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().name())
                        .andOnNotEmpty("name = ?", request.getNodeGroup())
                )
                .single();
        response.setResults(results.intValue());
        if (results == 0) {
            return response;
        }

        List<NodeGroupPo> rows = new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(
                        new WhereSql()
                        .andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().name())
                        .andOnNotEmpty("name = ?", request.getNodeGroup())
                )
                .orderBy()
                .column("gmt_created", OrderByType.DESC)
                .limit(request.getStart(), request.getLimit())
                .list(RshHolder.NODE_GROUP_LIST_RSH);

        response.setRows(rows);

        return response;
    }

    private String getTableName() {
        return JobQueueUtils.NODE_GROUP_STORE;
    }
}
