//package com.lts.job.core.registry.multcast;
//
//import com.lts.job.core.cluster.Config;
//import com.lts.job.core.cluster.Node;
//import com.lts.job.core.cluster.NodeType;
//import com.lts.job.core.constant.Constants;
//import com.lts.job.core.logger.Logger;
//import com.lts.job.core.logger.LoggerFactory;
//import com.lts.job.core.registry.FailbackRegistry;
//import com.lts.job.core.registry.NodeRegistryUtils;
//import com.lts.job.core.registry.NotifyEvent;
//import com.lts.job.core.registry.NotifyListener;
//import com.lts.job.core.util.ConcurrentHashSet;
//import com.lts.job.core.util.NetUtils;
//import com.lts.job.core.util.StringUtils;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.MulticastSocket;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * @author Robert HG (254963746@qq.com) on 5/19/15.
// */
//public class MulticastRegistry extends FailbackRegistry {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MulticastRegistry.class);
//
//    private static final int DEFAULT_MULTICAST_PORT = 1234;
//
//    private final ConcurrentMap<Node, Set<Node>> received = new ConcurrentHashMap<Node, Set<Node>>();
//
//    private final InetAddress multicastAddress;
//    private final MulticastSocket multicastSocket;
//    private final int multicastPort;
//
//    public MulticastRegistry(Config config) {
//        super(config);
//        String host = config.getRegistryAddress().split(":")[0];
//        Integer port = Integer.parseInt(config.getRegistryAddress().split(":")[1]);
//        if (!isMulticastAddress(host)) {
//            throw new IllegalArgumentException("Invalid multicast address " + host + ", scope: 224.0.0.0 - 239.255.255.255");
//        }
//
//        try {
//            multicastAddress = InetAddress.getByName(host);
//            multicastPort = port <= 0 ? DEFAULT_MULTICAST_PORT : port;
//            multicastSocket = new MulticastSocket(multicastPort);
//            multicastSocket.setLoopbackMode(false);
//            multicastSocket.joinGroup(multicastAddress);
//            Thread thread = new Thread(new Runnable() {
//                public void run() {
//                    byte[] buf = new byte[2048];
//                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
//                    while (!multicastSocket.isClosed()) {
//                        try {
//                            multicastSocket.receive(packet);
//                            String msg = new String(packet.getData()).trim();
//                            int i = msg.indexOf('\n');
//                            if (i > 0) {
//                                msg = msg.substring(0, i).trim();
//                            }
//                            MulticastRegistry.this.receive(msg, (InetSocketAddress) packet.getSocketAddress());
//                            Arrays.fill(buf, (byte) 0);
//                        } catch (Throwable e) {
//                            if (!multicastSocket.isClosed()) {
//                                LOGGER.error(e.getMessage(), e);
//                            }
//                        }
//                    }
//                }
//            }, "LTSMulticastRegistryReceiver");
//            thread.setDaemon(true);
//            thread.start();
//        } catch (IOException e) {
//            throw new IllegalStateException(e.getMessage(), e);
//        }
//    }
//
//    private void receive(String msg, InetSocketAddress remoteAddress) {
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info("Receive multicast message: " + msg + " from " + remoteAddress);
//        }
//        if (msg.startsWith(Constants.REGISTER)) {
//            Node node = NodeRegistryUtils.parse(msg.substring(Constants.REGISTER.length()).trim());
//            registered(node);
//        } else if (msg.startsWith(Constants.UNREGISTER)) {
//            Node node = NodeRegistryUtils.parse(msg.substring(Constants.UNREGISTER.length()).trim());
//            unregistered(node);
//        } else if (msg.startsWith(Constants.SUBSCRIBE)) {
//            Node node = NodeRegistryUtils.parse(msg.substring(Constants.SUBSCRIBE.length()).trim());
//            Set<Node> nodes = getRegistered();
//            if (nodes != null && nodes.size() > 0) {
//                for (Node u : nodes) {
//                    if (UrlUtils.isMatch(node, u)) {
//                        String host = remoteAddress != null && remoteAddress.getAddress() != null
//                                ? remoteAddress.getAddress().getHostAddress() : node.getIp();
//                        if (!NetUtils.getLocalHost().equals(host)) { // 同机器多进程不能用unicast单播信息，否则只会有一个进程收到信息
//                            unicast(Constants.REGISTER + " " + node.toFullString(), host);
//                        } else {
//                            broadcast(Constants.REGISTER + " " + node.toFullString());
//                        }
//                    }
//                }
//            }
//        }/* else if (msg.startsWith(UNSUBSCRIBE)) {
//        }*/
//    }
//
//    @Override
//    protected void doRegister(Node node) {
//        broadcast(Constants.REGISTER + " " + node.toFullString());
//    }
//
//    @Override
//    protected void doUnRegister(Node node) {
//        broadcast(Constants.UNREGISTER + " " + node.toFullString());
//    }
//
//    @Override
//    protected void doSubscribe(Node node, NotifyListener listener) {
//        broadcast(Constants.SUBSCRIBE + " " + node.toFullString());
//        synchronized (listener) {
//            try {
//                listener.wait(config.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
//            } catch (InterruptedException e) {
//            }
//        }
//    }
//
//    @Override
//    protected void doUnsubscribe(Node node, NotifyListener listener) {
//        broadcast(Constants.UNSUBSCRIBE + " " + node.toFullString());
//    }
//
//    protected void registered(Node node) {
//        for (Map.Entry<Node, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
//            Node key = entry.getKey();
//            if (isMySubscribed(key, node)) {
//                Set<Node> nodes = received.get(key);
//                if (nodes == null) {
//                    received.putIfAbsent(key, new ConcurrentHashSet<Node>());
//                    nodes = received.get(key);
//                }
//                nodes.add(node);
//                List<Node> list = toList(nodes);
//                for (NotifyListener listener : entry.getValue()) {
//                    notify(NotifyEvent.ADD, list, listener);
//                    synchronized (listener) {
//                        listener.notify();
//                    }
//                }
//            }
//        }
//    }
//
//    private boolean isMySubscribed(Node subscribed, Node node) {
//        // 是否是我订阅的
//        List<NodeType> listenNodeTypes = subscribed.getListenNodeTypes();
//        for (NodeType listenNodeType : listenNodeTypes) {
//            if (node.getNodeType().equals(listenNodeType)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    protected void unregistered(Node node) {
//        for (Map.Entry<Node, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
//            Node key = entry.getKey();
//            if (isMySubscribed(key, node)) {
//                Set<Node> nodes = received.get(key);
//                if (nodes != null) {
//                    nodes.remove(node);
//                }
//                List<Node> list = toList(nodes);
//                for (NotifyListener listener : entry.getValue()) {
//                    notify(NotifyEvent.REMOVE, list, listener);
//                }
//            }
//        }
//    }
//
//    protected void subscribed(Node node, NotifyListener listener) {
//        List<Node> nodes = lookup(node);
//        notify(node, listener, nodes);
//    }
//
//    private void unicast(String msg, String host) {
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info("Send unicast message: " + msg + " to " + host + ":" + multicastPort);
//        }
//        try {
//            byte[] data = (msg + "\n").getBytes();
//            DatagramPacket hi = new DatagramPacket(data, data.length, InetAddress.getByName(host), multicastPort);
//            multicastSocket.send(hi);
//        } catch (Exception e) {
//            throw new IllegalStateException(e.getMessage(), e);
//        }
//    }
//
//    private void broadcast(String msg) {
//        if (LOGGER.isInfoEnabled()) {
//            LOGGER.info("Send broadcast message: " + msg + " to " + multicastAddress + ":" + multicastPort);
//        }
//        try {
//            byte[] data = (msg + "\n").getBytes();
//            DatagramPacket hi = new DatagramPacket(data, data.length, multicastAddress, multicastPort);
//            multicastSocket.send(hi);
//        } catch (Exception e) {
//            throw new IllegalStateException(e.getMessage(), e);
//        }
//    }
//
//    private List<Node> toList(Set<Node> nodes) {
//        List<Node> list = new ArrayList<Node>();
//        if (nodes != null && nodes.size() > 0) {
//            for (Node node : nodes) {
//                list.add(node);
//            }
//        }
//        return list;
//    }
//
//    private static boolean isMulticastAddress(String ip) {
//        int i = ip.indexOf('.');
//        if (i > 0) {
//            String prefix = ip.substring(0, i);
//            if (StringUtils.isInteger(prefix)) {
//                int p = Integer.parseInt(prefix);
//                return p >= 224 && p <= 239;
//            }
//        }
//        return false;
//    }
//
//    public void destroy() {
//        super.destroy();
//        try {
//            multicastSocket.leaveGroup(multicastAddress);
//            multicastSocket.close();
//        } catch (Throwable t) {
//            LOGGER.warn(t.getMessage(), t);
//        }
//    }
//
//    public void register(Node node) {
//        super.register(node);
//        registered(node);
//    }
//
//    public void unregister(Node node) {
//        super.unregister(node);
//        unregistered(node);
//    }
//
//    public void subscribe(Node node, NotifyListener listener) {
//        super.subscribe(node, listener);
//        subscribed(node, listener);
//    }
//
//    public void unsubscribe(Node node, NotifyListener listener) {
//        super.unsubscribe(node, listener);
//        received.remove(node);
//    }
//
//    public List<Node> lookup(Node node) {
//        List<Node> nodes = new ArrayList<Node>();
//        Map<String, List<Node>> notifiedUrls = getNotified().get(node);
//        if (notifiedUrls != null && notifiedUrls.size() > 0) {
//            for (List<Node> values : notifiedUrls.values()) {
//                nodes.addAll(values);
//            }
//        }
//        if (nodes == null || nodes.size() == 0) {
//            for (Node u : getRegistered()) {
//                if (UrlUtils.isMatch(node, u)) {
//                    nodes.add(u);
//                }
//            }
//        }
//        return nodes;
//    }
//
//}
