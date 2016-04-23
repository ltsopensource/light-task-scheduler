//package com.github.ltsopensource.core;
//
//import com.github.ltsopensource.core.cluster.Node;
//import com.github.ltsopensource.core.commons.utils.StringUtils;
//import com.github.ltsopensource.core.constant.Constants;
//import com.github.ltsopensource.core.factory.NamedThreadFactory;
//import com.github.ltsopensource.core.logger.Logger;
//import com.github.ltsopensource.core.logger.LoggerFactory;
//import com.github.ltsopensource.core.registry.FailbackRegistry;
//import com.github.ltsopensource.core.registry.NodeRegistryUtils;
//import com.github.ltsopensource.core.registry.NotifyListener;
//
//import java.io.IOException;
//import java.net.*;
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * @author Robert HG (254963746@qq.com) on 9/10/15.
// */
//public class MulticastRegistry extends FailbackRegistry {
//
//    // 日志输出
//    private static final Logger logger = LoggerFactory.getLogger(MulticastRegistry.class);
//
//    private static final int DEFAULT_MULTICAST_PORT = 1234;
//
//    private final InetAddress multicastAddress;
//
//    private final MulticastSocket multicastSocket;
//
//    private final int multicastPort;
//
//    private final ConcurrentMap<Node, Set<Node>> received = new ConcurrentHashMap<Node, Set<Node>>();
//
//    private final ScheduledExecutorService cleanExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTSMulticastRegistryCleanTimer", true));
//
//    private final ScheduledFuture<?> cleanFuture;
//
//    private final int cleanPeriod;
//
//    public MulticastRegistry(Application application) {
//        super(application);
//
//        String address = NodeRegistryUtils.getRealRegistryAddress(application.getConfig().getRegistryAddress());
//        String host = address.split(":")[0];
//        Integer port = Integer.parseInt(address.split(":")[1]);
//
//        if (!isMulticastAddress(host)) {
//            throw new IllegalArgumentException("Invalid multicast address " + host + ", scope: 224.0.0.0 - 239.255.255.255");
//        }
//        try {
//            multicastAddress = InetAddress.getByName(host);
//            multicastPort = port <= 0 ? DEFAULT_MULTICAST_PORT : port;
//            multicastSocket = new MulticastSocket(multicastPort);
//            multicastSocket.setLoopbackMode(false);
//            multicastSocket.joinGroup(multicastAddress);
//            Thread thread = new Thread(new Runnable() {
//                public void run() {
//                    byte[] buf = new byte[2048];
//                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
//                    while (!multicastSocket.isClosed()) {
//                        try {
//                            multicastSocket.receive(recv);
//                            String msg = new String(recv.getData()).trim();
//                            int i = msg.indexOf('\n');
//                            if (i > 0) {
//                                msg = msg.substring(0, i).trim();
//                            }
//                            MulticastRegistry.this.receive(msg, (InetSocketAddress) recv.getSocketAddress());
//                            Arrays.fill(buf, (byte) 0);
//                        } catch (Throwable e) {
//                            if (!multicastSocket.isClosed()) {
//                                logger.error(e.getMessage(), e);
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
//
//        this.cleanPeriod = application.getConfig().getParameter(Constants.REDIS_SESSION_TIMEOUT, Constants.DEFAULT_SESSION_TIMEOUT);
//
//        boolean admin = true;
//        if (admin) {
//            this.cleanFuture = cleanExecutor.scheduleWithFixedDelay(new Runnable() {
//                public void run() {
//                    try {
//                        clean(); // 清除过期者
//                    } catch (Throwable t) { // 防御性容错
//                        logger.error("Unexpected exception occur at clean expired provider, cause: " + t.getMessage(), t);
//                    }
//                }
//            }, cleanPeriod, cleanPeriod, TimeUnit.MILLISECONDS);
//        } else {
//            this.cleanFuture = null;
//        }
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
//    private void clean() {
//        for (Set<Node> providers : new HashSet<Set<Node>>(received.values())) {
//            for (Node node : new HashSet<Node>(providers)) {
//                if (isExpired(node)) {
//                    if (logger.isWarnEnabled()) {
//                        logger.warn("Clean expired provider " + node);
//                    }
//                    doUnregister(node);
//                }
//            }
//        }
//    }
//
//    private boolean isExpired(Node node) {
//
//        Socket socket = null;
//        try {
//            socket = new Socket(node.getIp(), node.getPort());
//        } catch (Throwable e) {
//            try {
//                Thread.sleep(100);
//            } catch (Throwable ignored) {
//            }
//            Socket socket2 = null;
//            try {
//                socket2 = new Socket(node.getIp(), node.getPort());
//            } catch (Throwable e2) {
//                return true;
//            } finally {
//                if (socket2 != null) {
//                    try {
//                        socket2.close();
//                    } catch (Throwable ignored) {
//                    }
//                }
//            }
//        } finally {
//            if (socket != null) {
//                try {
//                    socket.close();
//                } catch (Throwable ignored) {
//                }
//            }
//        }
//        return false;
//    }
//
//    private void receive(String msg, InetSocketAddress remoteAddress) {
//        if (logger.isInfoEnabled()) {
//            logger.info("Receive multicast message: " + msg + " from " + remoteAddress);
//        }
//        if (msg.startsWith(Constants.REGISTER)) {
//            URL url = URL.valueOf(msg.substring(Constants.REGISTER.length()).trim());
//            registered(url);
//        } else if (msg.startsWith(Constants.UNREGISTER)) {
//            URL url = URL.valueOf(msg.substring(Constants.UNREGISTER.length()).trim());
//            unregistered(url);
//        } else if (msg.startsWith(Constants.SUBSCRIBE)) {
//            URL url = URL.valueOf(msg.substring(Constants.SUBSCRIBE.length()).trim());
//            Set<Node> urls = getRegistered();
//            if (urls != null && urls.size() > 0) {
//                for (URL u : urls) {
//                    if (UrlUtils.isMatch(url, u)) {
//                        String host = remoteAddress != null && remoteAddress.getAddress() != null
//                                ? remoteAddress.getAddress().getHostAddress() : url.getIp();
//                        broadcast(Constants.REGISTER + " " + u.toFullString());
//                    }
//                }
//            }
//        }/* else if (msg.startsWith(UNSUBSCRIBE)) {
//        }*/
//    }
//
//    private void broadcast(String msg) {
//        if (logger.isInfoEnabled()) {
//            logger.info("Send broadcast message: " + msg + " to " + multicastAddress + ":" + multicastPort);
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
//    protected void doRegister(Node node) {
//        broadcast(Constants.REGISTER + " " + node.toFullString());
//    }
//
//    protected void doUnregister(Node node) {
//        broadcast(Constants.UNREGISTER + " " + node.toFullString
//
//    protected void doSubscribe(URL url, NotifyListener listener) {
//        if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
//            admin = true;
//        }
//        broadcast(Constants.SUBSCRIBE + " " + url.toFullString());
//        synchronized (listener) {
//            try {
//                listener.wait(url.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
//            } catch (InterruptedException ignored) {
//            }
//        }
//    }
//
//    protected void doUnsubscribe(URL url, NotifyListener listener) {
//        if (!Constants.ANY_VALUE.equals(url.getServiceInterface())
//                && url.getParameter(Constants.REGISTER_KEY, true)) {
//            unregister(url);
//        }
//        broadcast(Constants.UNSUBSCRIBE + " " + url.toFullString());
//    }
//
//    public boolean isAvailable() {
//        try {
//            return multicastSocket != null;
//        } catch (Throwable t) {
//            return false;
//        }
//    }
//
//    public void destroy() {
//        super.destroy();
//        try {
//            if (cleanFuture != null) {
//                cleanFuture.cancel(true);
//            }
//        } catch (Throwable t) {
//            logger.warn(t.getMessage(), t);
//        }
//        try {
//            multicastSocket.leaveGroup(multicastAddress);
//            multicastSocket.close();
//        } catch (Throwable t) {
//            logger.warn(t.getMessage(), t);
//        }
//    }
//
//    @Override
//    protected void doUnRegister(Node node) {
//
//    }
//
//    @Override
//    protected void doSubscribe(Node node, NotifyListener listener) {
//
//    }
//
//    @Override
//    protected void doUnsubscribe(Node node, NotifyListener listener) {
//
//    }
//
//    protected void registered(URL url) {
//        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
//            URL key = entry.getKey();
//            if (UrlUtils.isMatch(key, url)) {
//                Set<URL> urls = received.get(key);
//                if (urls == null) {
//                    received.putIfAbsent(key, new ConcurrentHashSet<URL>());
//                    urls = received.get(key);
//                }
//                urls.add(url);
//                List<URL> list = toList(urls);
//                for (NotifyListener listener : entry.getValue()) {
//                    notify(key, listener, list);
//                    synchronized (listener) {
//                        listener.notify();
//                    }
//                }
//            }
//        }
//    }
//
//    protected void unregistered(URL url) {
//        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
//            URL key = entry.getKey();
//            if (UrlUtils.isMatch(key, url)) {
//                Set<URL> urls = received.get(key);
//                if (urls != null) {
//                    urls.remove(url);
//                }
//                List<URL> list = toList(urls);
//                for (NotifyListener listener : entry.getValue()) {
//                    notify(key, listener, list);
//                }
//            }
//        }
//    }
//
//    protected void subscribed(URL url, NotifyListener listener) {
//        List<URL> urls = lookup(url);
//        notify(url, listener, urls);
//    }
//
//    private List<URL> toList(Set<URL> urls) {
//        List<URL> list = new ArrayList<URL>();
//        if (urls != null && urls.size() > 0) {
//            for (URL url : urls) {
//                list.add(url);
//            }
//        }
//        return list;
//    }
//
//    public void register(URL url) {
//        super.register(url);
//        registered(url);
//    }
//
//    public void unregister(URL url) {
//        super.unregister(url);
//        unregistered(url);
//    }
//
//    public void subscribe(URL url, NotifyListener listener) {
//        super.subscribe(url, listener);
//        subscribed(url, listener);
//    }
//
//    public void unsubscribe(URL url, NotifyListener listener) {
//        super.unsubscribe(url, listener);
//        received.remove(url);
//    }
//
//    public List<URL> lookup(URL url) {
//        List<URL> urls = new ArrayList<URL>();
//        Map<String, List<URL>> notifiedUrls = getNotified().get(url);
//        if (notifiedUrls != null && notifiedUrls.size() > 0) {
//            for (List<URL> values : notifiedUrls.values()) {
//                urls.addAll(values);
//            }
//        }
//        if (urls == null || urls.size() == 0) {
//            List<URL> cacheUrls = getCacheUrls(url);
//            if (cacheUrls != null && cacheUrls.size() > 0) {
//                urls.addAll(cacheUrls);
//            }
//        }
//        if (urls == null || urls.size() == 0) {
//            for (URL u : getRegistered()) {
//                if (UrlUtils.isMatch(url, u)) {
//                    urls.add(u);
//                }
//            }
//        }
//        if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
//            for (URL u : getSubscribed().keySet()) {
//                if (UrlUtils.isMatch(url, u)) {
//                    urls.add(u);
//                }
//            }
//        }
//        return urls;
//    }
//
//    public MulticastSocket getMutilcastSocket() {
//        return multicastSocket;
//    }
//
//    public Map<Node, Set<Node>> getReceived() {
//        return received;
//    }
//
//}
