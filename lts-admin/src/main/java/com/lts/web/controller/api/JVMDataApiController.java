package com.lts.web.controller.api;

import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.web.cluster.RegistryService;
import com.lts.web.controller.AbstractController;
import com.lts.web.repository.domain.JVMInfoDataPo;
import com.lts.web.repository.mapper.JVMInfoRepo;
import com.lts.web.request.JVMDataRequest;
import com.lts.web.request.NodeRequest;
import com.lts.web.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
@RestController
@RequestMapping("/jvm")
public class JVMDataApiController extends AbstractController {

    @Autowired
    JVMInfoRepo jvmInfoRepo;
    @Autowired
    RegistryService registryService;

    @RequestMapping("node-jvm-info-get")
    public RestfulResponse getNodeList(JVMDataRequest request) {
        RestfulResponse response = new RestfulResponse();

        JVMInfoDataPo data = jvmInfoRepo.select(request);

        if (data != null) {
            NodeRequest nodeRequest = new NodeRequest();
            nodeRequest.setIdentity(request.getIdentity());
            List<Node> nodes = registryService.getOnlineNodes(nodeRequest);
            boolean alive = CollectionUtils.sizeOf(nodes) > 0;
            data.setAlive(alive);
            response.setResults(1);
            response.setRows(Collections.singletonList(data));
        }
        response.setSuccess(true);

        return response;
    }

}
