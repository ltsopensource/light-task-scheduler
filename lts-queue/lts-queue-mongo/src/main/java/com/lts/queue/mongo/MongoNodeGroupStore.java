package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.commons.utils.DateUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.NodeGroupStore;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.store.mongo.MongoRepository;
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
        if (CollectionUtils.isEmpty(indexInfo)) {
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
}
