package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.NodeGroupGetReq;
import com.github.ltsopensource.core.support.JobQueueUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.NodeGroupStore;
import com.github.ltsopensource.queue.domain.NodeGroupPo;
import com.github.ltsopensource.store.mongo.MongoRepository;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class MongoNodeGroupStore extends MongoRepository implements NodeGroupStore {

    public MongoNodeGroupStore(Config config) {
        super(config);
        setTableName(JobQueueUtils.NODE_GROUP_STORE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_nodeType_name", "nodeType,name", true, true);
        }
    }

    @Override
    public void addNodeGroup(NodeType nodeType, String name) {
        try {
            NodeGroupPo nodeGroupPo = new NodeGroupPo();
            nodeGroupPo.setNodeType(nodeType);
            nodeGroupPo.setName(name);
            nodeGroupPo.setGmtCreated(SystemClock.now());
            template.save(nodeGroupPo);
        } catch (DuplicateKeyException e) {
            // ignore
        }
    }

    @Override
    public void removeNodeGroup(NodeType nodeType, String name) {
        Query<NodeGroupPo> query = template.createQuery(NodeGroupPo.class);
        query.field("nodeType").equal(nodeType).field("name").equal(name);
        template.delete(query);
    }

    @Override
    public List<NodeGroupPo> getNodeGroup(NodeType nodeType) {
        Query<NodeGroupPo> query = template.createQuery(NodeGroupPo.class);
        query.field("nodeType").equal(nodeType);
        return query.asList();
    }

    @Override
    public PaginationRsp<NodeGroupPo> getNodeGroup(NodeGroupGetReq request) {
        Query<NodeGroupPo> query = template.createQuery(NodeGroupPo.class);
        if (request.getNodeType() != null) {
            query.field("nodeType").equal(request.getNodeType());
        }
        if (StringUtils.isNotEmpty(request.getNodeGroup())) {
            query.field("name").equal(request.getNodeGroup());
        }
        PaginationRsp<NodeGroupPo> response = new PaginationRsp<NodeGroupPo>();
        Long results = template.getCount(query);
        response.setResults(results.intValue());
        if (results == 0) {
            return response;
        }
        query.order("-gmtCreated").offset(request.getStart()).limit(request.getLimit());

        response.setRows(query.asList());
        return response;
    }
}
