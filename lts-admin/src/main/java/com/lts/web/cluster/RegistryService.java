package com.lts.web.cluster;

import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.registry.NotifyEvent;
import com.lts.core.registry.NotifyListener;
import com.lts.core.registry.Registry;
import com.lts.core.registry.RegistryFactory;
import com.lts.web.repository.memory.NodeMemoryDatabase;
import com.lts.web.request.NodeRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class RegistryService implements InitializingBean {

    private NodeMemoryDatabase repo = new NodeMemoryDatabase();

    @Autowired
    private AdminApplication application;

    private Registry registry;

    private void register() {

        registry.subscribe(application.getNode(), new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        repo.addNode(nodes);
                        break;
                    case REMOVE:
                        repo.removeNode(nodes);
                        break;
                }
            }
        });
    }

    public List<Node> getNodes(NodeRequest request) {
        return repo.search(request);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        registry = RegistryFactory.getRegistry(application);
        
        register();
    }
}
