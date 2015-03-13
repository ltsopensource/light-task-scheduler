package com.lts.job.core;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeFactory;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.registry.NodeRegistry;
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

    }

    public class JobClientNode extends Node {

        public JobClientNode() {
            super();
            this.setNodeType(NodeType.CLIENT);
            this.addListenNodeType(NodeType.JOB_TRACKER);
        }

    }

}
