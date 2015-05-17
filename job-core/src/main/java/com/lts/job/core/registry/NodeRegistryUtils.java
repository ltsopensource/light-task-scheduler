package com.lts.job.core.registry;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 *         <p/>
 *         /LTS/{集群名字}/NODES/JOB_TRACKER/JOB_TRACKER:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         <p/>
 */
public class NodeRegistryUtils {

    public static String getRootPath(String clusterName) {
        return "/LTS/" + clusterName + "/NODES";
    }

    public static String getNodeTypePath(String clusterName, NodeType nodeType) {
        return NodeRegistryUtils.getRootPath(clusterName) + "/" + nodeType;
    }

    public static Node parse(String clusterName, String fullPath) {
        Node node = new Node();
        String nodeType = getMatcher(getRootPath(clusterName) + "/(.*)/", fullPath);
        node.setNodeType(NodeType.valueOf(nodeType));

        String url = getMatcher(getRootPath(clusterName) + "/" + nodeType + "/" + nodeType + ":\\\\\\\\(.*)", fullPath);

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
            String key = paramEntry.split("=")[0];
            String value = paramEntry.split("=")[1];

            if ("group".equals(key)) {
                node.setGroup(value);
            } else if ("threads".equals(key)) {
                node.setThreads(Integer.valueOf(value));
            } else if ("identity".equals(key)) {
                node.setIdentity(value);
            } else if ("createTime".equals(key)) {
                node.setCreateTime(Long.valueOf(value));
            } else if ("isAvailable".equals(key)) {
                node.setAvailable(Boolean.valueOf(value));
            } else if ("listenNodeTypes".equals(key)) {
                String[] nodeTypes = value.split(",");
                for (String type : nodeTypes) {
                    node.addListenNodeType(NodeType.valueOf(type));
                }
            }
        }
        return node;
    }

    public static String getFullPath(String clusterName, Node node) {
        StringBuilder path = new StringBuilder();
        path.append(getRootPath(clusterName))
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
                .append(node.getGroup());
        if (node.getThreads() != 0) {
            path.append("&threads=")
                    .append(node.getThreads());
        }

        path.append("&identity=")
                .append(node.getIdentity())
                .append("&createTime=")
                .append(node.getCreateTime())
                .append("&isAvailable=")
                .append(node.isAvailable());

        if (node.getListenNodeTypes() != null && node.getListenNodeTypes().size() != 0) {
            path.append("&listenNodeTypes=");
            for (NodeType nodeType : node.getListenNodeTypes()) {
                path.append(nodeType).append(",");
            }
            path.deleteCharAt(path.length() - 1);
        }
        return path.toString();
    }

    private static String getMatcher(String regex, String source) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            return matcher.group(1);//只取第一组
        }
        return "";
    }
}
