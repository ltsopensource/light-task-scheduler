package com.lts.core.command;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主要用于 curl
 *
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCommandCenter {

    private final Logger LOGGER = LoggerFactory.getLogger(HttpCommandCenter.class);
    private ExecutorService commandExecutor;
    private AtomicBoolean start = new AtomicBoolean(false);
    private final Map<String, HttpCommandProcessor> processorMap = new HashMap<String, HttpCommandProcessor>();

    private Config config;
    private int port;

    public HttpCommandCenter(Config config) {
        this.config = config;
    }

    public void start() throws HttpCommandException {
        try {
            if (start.compareAndSet(false, true)) {

                port = config.getParameter("lts.command.port", 8719);

                commandExecutor = new ThreadPoolExecutor(Constants.AVAILABLE_PROCESSOR,
                        Constants.AVAILABLE_PROCESSOR,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.DiscardPolicy());

                ServerSocket serverSocket = getServerSocket();
                // 开启监听命令
                startServerListener(serverSocket);

                LOGGER.info("Start CommandCenter succeed at port {}", port);
            }
        } catch (Exception t) {
            LOGGER.error("Start CommandCenter error at port {} , use lts.command.port config change the port.", port, t);
            throw new HttpCommandException(t);
        }
    }

    private ServerSocket getServerSocket() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (BindException e) {
            if (e.getMessage().contains("Address already in use")) {
                port = port + 1;
                serverSocket = getServerSocket();
            } else {
                throw e;
            }
        }
        return serverSocket;
    }

    private void startServerListener(final ServerSocket serverSocket) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (start.get()) {
                    Socket socket = null;

                    try {
                        socket = serverSocket.accept();
                        if (socket == null) {
                            continue;
                        }
                        commandExecutor.submit(new EventRunnable(socket));

                    } catch (Throwable t) {
                        LOGGER.error("Accept error ", t);

                        try {
                            Thread.sleep(1000); // 1s
                        } catch (InterruptedException ignored) {
                        }
                    }
                }

            }
        }).start();
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                commandExecutor.shutdownNow();
                LOGGER.info("Stop CommandCenter succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop CommandCenter error ", t);
        }
    }

    public int getPort() {
        return port;
    }

    public void registerCommand(String command, HttpCommandProcessor processor) {

        if (StringUtils.isEmpty(command)) {
            return;
        }
        processorMap.put(command, processor);
    }

    private HttpCommandProcessor getCommandProcessor(String command) {
        return processorMap.get(command);
    }

    class EventRunnable implements Runnable {

        private Socket socket;

        public EventRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            BufferedReader in = null;
            PrintWriter out = null;

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream);

                String line = in.readLine();
                HttpCommandRequest request = HttpCommandRequest.parse(line);

                out.print("HTTP/1.1 200 OK\r\n\r\n");
                out.flush();
                if (StringUtils.isEmpty(request.getCommand())) {
                    out.println("Command is blank");
                    out.flush();
                    return;
                }

                HttpCommandProcessor httpCommandProcessor = getCommandProcessor(request.getCommand());
                if (httpCommandProcessor != null) {
                    httpCommandProcessor.execute(outputStream, request);
                } else {
                    out.println("Can not find the command:[" + request.getCommand() + "]");
                }
                out.flush();

            } catch (Throwable t) {
                LOGGER.error("EventRunnable error", t);

                try {
                    if (out != null) {
                        out.println("CommandCenter error, message is " + t.getMessage());
                        out.flush();
                    }
                } catch (Exception e) {
                    LOGGER.error("EventRunnable error", t);
                }

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    socket.close();
                } catch (Exception e) {
                    LOGGER.error("EventRunnable close resource error", e);
                }
            }

        }
    }

}
