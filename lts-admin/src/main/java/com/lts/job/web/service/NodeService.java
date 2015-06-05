package com.lts.job.web.service;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.util.StringUtils;
import com.lts.job.web.cluster.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class NodeService {

    @Autowired
    RegistryService registryService;

    public List<Node> getNodeList(String clusterName) {

        registryService.register(clusterName);

        if (StringUtils.isNotEmpty(clusterName)) {
            return registryService.getAllNodes(clusterName);
        } else {
            return registryService.getAllNodes();
        }
    }

}
