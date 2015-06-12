package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.commons.utils.CollectionUtils;
import com.lts.job.core.commons.utils.DateUtils;
import com.lts.job.core.support.JobQueueUtils;
import com.lts.job.queue.NodeGroupStore;
import com.lts.job.queue.NodeGroupStoreFactory;
import com.lts.job.queue.domain.NodeGroupPo;
import com.lts.job.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public class MongoNodeGroupStoreFactory implements NodeGroupStoreFactory {

    @Override
    public NodeGroupStore getStore(Config config) {
        return new MongoNodeGroupStore(config);
    }
}
