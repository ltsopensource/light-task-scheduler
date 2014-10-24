package com.lts.job.common;

import com.lts.job.common.cluster.Node;
import com.lts.job.common.cluster.NodeFactory;
import com.lts.job.common.cluster.NodeType;
import com.lts.job.common.domain.JobNodeConfig;
import com.lts.job.common.registry.NodeRegistry;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class RegistryTest {

    @Test
    public void test_registry() throws IOException {

        String address = "localhost:2181";
        NodeRegistry registry = new NodeRegistry();

        List<NodeType> nodeTypeList = new ArrayList<NodeType>();
        nodeTypeList.add(NodeType.JOB_TRACKER);

        JobClientNode node = NodeFactory.create(JobClientNode.class, new JobNodeConfig());
        registry.register(node);

        System.in.read();
    }

    public class JobClientNode extends Node {

        public JobClientNode() {
            super();
            this.setNodeType(NodeType.CLIENT);
            this.addListenNodeType(NodeType.JOB_TRACKER);
        }

    }

}
