package com.github.ltsopensource.cmd;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主要用于 curl
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCmdServer {

    private final Logger LOGGER = LoggerFactory.getLogger(HttpCmdServer.class);

    private final AtomicBoolean start = new AtomicBoolean(false);
    private HttpCmdAcceptor acceptor;
    private int port;
    private String bindAddr;
    private HttpCmdContext context;
    private int portFindTimes;

    private HttpCmdServer(String bindAddr, int port) {
        this.port = port > 0 ? port : 8719;
        this.bindAddr = bindAddr;
        this.context = new HttpCmdContext();
    }

    public void start() throws HttpCmdException {
        try {
            if (start.compareAndSet(false, true)) {
                // 开启监听命令
                acceptor = new HttpCmdAcceptor(getServerSocket(), context);
                acceptor.start();
                LOGGER.info("\n===========================================================================================\n" +
                        "Start succeed at port {}\n" +
                        "===========================================================================================", port);
            }
        } catch (Exception t) {
            LOGGER.error("Start error at port {}", port, t);
            throw new HttpCmdException(t);
        }
    }

    private ServerSocket getServerSocket() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 100);
            serverSocket.setReuseAddress(true);
        } catch (BindException e) {
            port = port + 1;
            serverSocket = getServerSocket();
            if (portFindTimes++ > 50) {
                throw e;
            }
        }
        return serverSocket;
    }

    public void stop() {
        if (acceptor != null) {
            acceptor.stop();
        }
    }

    public int getPort() {
        return port;
    }

    public void registerCommand(HttpCmdProc proc) {
        context.addCmdProcessor(proc);
    }

    public void registerCommands(HttpCmdProc... procs) {
        if (procs != null && procs.length > 0) {
            for (HttpCmdProc proc : procs) {
                this.registerCommand(proc);
            }
        }
    }

    /**
     * 保证一个jvm公用一个 HttpCmdServer
     */
    public static class Factory {

        private static HttpCmdServer httpCmdServer;

        public static HttpCmdServer getHttpCmdServer(String bindAddr, int port) {
            if (httpCmdServer != null) {
                return httpCmdServer;
            }
            synchronized (Factory.class) {
                if (httpCmdServer != null) {
                    return httpCmdServer;
                }
                httpCmdServer = new HttpCmdServer(bindAddr, port);
                return httpCmdServer;
            }
        }
    }

}
