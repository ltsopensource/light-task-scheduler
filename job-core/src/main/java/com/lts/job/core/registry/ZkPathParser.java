package com.lts.job.core.registry;


import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 *         <p/>
 *         /LTS/{集群名字}/NODES/JOB_TRACKER/JOB_TRACKER:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         <p/>
 *         节点 zookeeper上路径解析工具
 */
public class ZkPathParser implements PathParser {

    private Application application;

    public ZkPathParser(Application application) {
        this.application = application;
    }

    public Node parse(String fullPath) {
        return ZkNodeUtils.parse(application.getConfig().getClusterName(), fullPath);
    }

    public String getPath(Node node) {
        return ZkNodeUtils.getPath(application.getConfig().getClusterName(), node);
    }

    public String getPath(NodeType nodeType) {
        return ZkNodeUtils.getBasePath(application.getConfig().getClusterName()) + "/" + nodeType;
    }

}
