package com.github.ltsopensource.core.registry;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 *         <p/>
 *         /LTS/{集群名字}/NODES/TASK_TRACKER/TASK_TRACKER:\\192.168.0.150:8888?group=TASK_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /LTS/{集群名字}/NODES/JOB_CLIENT/JOB_CLIENT:\\192.168.0.150:8888?group=JOB_CLIENT&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /LTS/{集群名字}/NODES/JOB_TRACKER/JOB_TRACKER:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /LTS/{集群名字}/NODES/MONITOR/MONITOR:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true
 *         <p/>
 */
public class NodeRegistryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistryUtils.class);

    public static String getRootPath(String clusterName) {
        return "/LTS/" + clusterName + "/NODES";
    }

    public static String getNodeTypePath(String clusterName, NodeType nodeType) {
        return NodeRegistryUtils.getRootPath(clusterName) + "/" + nodeType;
    }

    public static Node parse(String fullPath) {
        try {
            Node node = new Node();
            String[] nodeDir = fullPath.split("/");
            NodeType nodeType = NodeType.valueOf(nodeDir[4]);
            node.setNodeType(nodeType);
            String url = nodeDir[5];

            url = url.substring(nodeType.name().length() + 3);
            String address = url.split("\\?")[0];
            String ip = address.split(":")[0];

            node.setIp(ip);
            if (address.contains(":")) {
                String port = address.split(":")[1];
                if (port != null && !"".equals(port.trim())) {
                    node.setPort(Integer.valueOf(port));
                }
            }
            String params = url.split("\\?")[1];

            String[] paramArr = params.split("&");
            for (String paramEntry : paramArr) {
                if (StringUtils.isEmpty(paramEntry)) {
                    continue;
                }
                String key = paramEntry.split("=")[0];
                String value = paramEntry.split("=")[1];
                if ("clusterName".equals(key)) {
                    node.setClusterName(value);
                } else if ("group".equals(key)) {
                    node.setGroup(value);
                } else if ("threads".equals(key)) {
                    node.setThreads(Integer.valueOf(value));
                } else if ("identity".equals(key)) {
                    node.setIdentity(value);
                } else if ("createTime".equals(key)) {
                    node.setCreateTime(Long.valueOf(value));
                } else if ("isAvailable".equals(key)) {
                    node.setAvailable(Boolean.valueOf(value));
                } else if ("hostName".equals(key)) {
                    node.setHostName(value);
                } else if ("httpCmdPort".equals(key)) {
                    node.setHttpCmdPort(Integer.valueOf(value));
                }
            }
            return node;
        } catch (RuntimeException e) {
            LOGGER.error("Error parse node , path:" + fullPath);
            throw e;
        }
    }

    public static String getFullPath(Node node) {
        StringBuilder path = new StringBuilder();

        path.append(getRootPath(node.getClusterName()))
                .append("/")
                .append(node.getNodeType())
                .append("/")
                .append(node.getNodeType())
                .append(":\\\\")
                .append(node.getIp());

        if (node.getPort() != null && node.getPort() != 0) {
            path.append(":").append(node.getPort());
        }

        path.append("?")
                .append("group=")
                .append(node.getGroup())
                .append("&clusterName=")
                .append(node.getClusterName());
        if (node.getThreads() != 0) {
            path.append("&threads=")
                    .append(node.getThreads());
        }

        path.append("&identity=")
                .append(node.getIdentity())
                .append("&createTime=")
                .append(node.getCreateTime())
                .append("&isAvailable=")
                .append(node.isAvailable())
                .append("&hostName=")
                .append(node.getHostName());

        if (node.getHttpCmdPort() != null) {
            path.append("&httpCmdPort=").append(node.getHttpCmdPort());
        }

        return path.toString();
    }

    public static String getRealRegistryAddress(String registryAddress) {
        if (StringUtils.isEmpty(registryAddress)) {
            throw new IllegalArgumentException("registryAddress is null！");
        }
        if (registryAddress.startsWith("zookeeper://")) {
            return registryAddress.replace("zookeeper://", "");
        } else if (registryAddress.startsWith("redis://")) {
            return registryAddress.replace("redis://", "");
        } else if (registryAddress.startsWith("multicast://")) {
            return registryAddress.replace("multicast://", "");
        }
        throw new IllegalArgumentException("Illegal registry protocol");
    }

}
