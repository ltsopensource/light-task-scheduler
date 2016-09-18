package com.github.ltsopensource.cmd;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class HttpCmdAcceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCmdAcceptor.class);

    private final AtomicBoolean start = new AtomicBoolean(false);
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private Thread thread;
    private HttpCmdContext context;

    public HttpCmdAcceptor(ServerSocket serverSocket, HttpCmdContext context) {
        this.context = context;
        this.serverSocket = serverSocket;
        this.executorService = new ThreadPoolExecutor(Constants.AVAILABLE_PROCESSOR,
                Constants.AVAILABLE_PROCESSOR,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.DiscardPolicy());
    }

    public void start() {

        if (!start.compareAndSet(false, true)) {
            // 如果已经启动了,就不重复启动
            return;
        }

        if (thread == null) {
            this.thread = new NamedThreadFactory("HTTP-CMD-ACCEPTOR", true).newThread(
                    new Runnable() {
                        @Override
                        public void run() {

                            while (isStarted()) {
                                try {
                                    Socket socket = serverSocket.accept();
                                    if (socket == null) {
                                        continue;
                                    }
                                    executorService.submit(new HttpCmdExecutor(context, socket));

                                } catch (Throwable t) {
                                    LOGGER.error("Accept error ", t);
                                    try {
                                        Thread.sleep(1000); // 1s
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            }

                        }
                    }
            );
        }
        // 启动线程
        thread.start();

        LOGGER.info("HttpCmdAcceptor start succeed ");
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                executorService.shutdown();
                LOGGER.info("HttpCmdAcceptor stop succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("HttpCmdAcceptor stop error ", t);
        }
    }

    private boolean isStarted() {
        return start.get();
    }

}
