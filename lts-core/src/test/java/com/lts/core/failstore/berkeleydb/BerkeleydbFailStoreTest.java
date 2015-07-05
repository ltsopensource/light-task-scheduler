package com.lts.core.failstore.berkeleydb;

import com.lts.core.cluster.Config;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.domain.Job;
import com.lts.core.domain.KVPair;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.FailStoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 5/26/15.
 */
public class BerkeleydbFailStoreTest {

    FailStore failStore;

    private String key = "x2x3423412x";

    @Before
    public void setup() throws FailStoreException {
        Config config = new Config();
        config.setNodeGroup("berkeleydb_test");
        config.setNodeType(NodeType.JOB_CLIENT);
        config.setFailStorePath(Constants.USER_HOME);
        config.setIdentity(StringUtils.generateUUID());
        failStore = new BerkeleydbFailStore(config.getFailStorePath(), config.getIdentity());
        failStore.open();
    }

    @Test
    public void put() throws FailStoreException {
        Job job = new Job();
        job.setTaskId("2131232");
        for (int i = 0; i < 100; i++) {
            failStore.put(key + "" + i, job);
        }
        System.out.println("这里debug测试多线程");
        failStore.close();
    }

    @Test
    public void fetchTop() throws FailStoreException {
        List<KVPair<String, Job>> kvPairs = failStore.fetchTop(5, Job.class);
        if (CollectionUtils.isNotEmpty(kvPairs)) {
            for (KVPair<String, Job> kvPair : kvPairs) {
                System.out.println(JSONUtils.toJSONString(kvPair));
            }
        }
    }

//    @Test
//    public void del() throws FailStoreException {
//        failStore.delete(key);
//    }

}