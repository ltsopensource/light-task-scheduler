package com.lts.command;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class CommandCenter {

    private final Logger LOGGER = LoggerFactory.getLogger(CommandCenter.class);
    private ExecutorService commandExecutor;
    private AtomicBoolean start = new AtomicBoolean(false);
    private final Map<String, CommandProcessor> processorMap = new HashMap<String, CommandProcessor>();

    private Config config;

    public CommandCenter(Config config) {
        this.config = config;
    }

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {

                int commandPort = config.getParameter("lts.command.port", 8719);
                commandExecutor = new ThreadPoolExecutor(Constants.AVAILABLE_PROCESSOR,
                        Constants.AVAILABLE_PROCESSOR,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(100), new ThreadPoolExecutor.DiscardPolicy());

                ServerSocket serverSocket = new ServerSocket(commandPort);
                // 开启监听命令
                startServerListener(serverSocket);

                LOGGER.info("Start CommandCenter succeed at port {}", serverSocket.getLocalPort());
            }
        } catch (Throwable t) {
            LOGGER.error("Start CommandCenter error ", t);
        }
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

    public void registerCommand(String command, CommandProcessor processor) {

        if (StringUtils.isEmpty(command)) {
            return;
        }
        processorMap.put(command, processor);
    }

    private CommandProcessor getCommandProcessor(String command) {
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
                CommandRequest request = CommandRequestParser.parse(line);

                out.print("HTTP/1.1 200 OK\r\n\r\n");
                out.flush();
                if (StringUtils.isEmpty(request.getCommand())) {
                    out.println("Command is blank");
                    out.flush();
                    return;
                }

                CommandProcessor commandProcessor = getCommandProcessor(request.getCommand());
                if (commandProcessor != null) {
                    commandProcessor.execute(outputStream, request);
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
