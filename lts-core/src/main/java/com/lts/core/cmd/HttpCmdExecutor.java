package com.lts.core.cmd;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class HttpCmdExecutor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCmdExecutor.class);
    private HttpCmdContext context;
    private Socket socket;

    private BufferedReader in = null;
    private PrintWriter out = null;

    public HttpCmdExecutor(HttpCmdContext context, Socket socket) {
        this.context = context;
        this.socket = socket;
    }

    @Override
    public void run() {

        HttpCmdResponse response = null;

        try {
            // 解析请求
            HttpCmdRequest request = parseRequest();

            if (StringUtils.isEmpty(request.getCommand())) {
                response = HttpCmdResponse.newResponse(false, "Command is blank");
                return;
            }

            HttpCmdProcessor httpCmdProcessor = context.getCmdProcessor(request.getCommand());

            if (httpCmdProcessor != null) {
                response = httpCmdProcessor.execute(request);
            } else {
                response = HttpCmdResponse.newResponse(false, "Can not find the command:[" + request.getCommand() + "]");
            }
        } catch (Throwable t) {
            LOGGER.error("Execute command error", t);
            response = HttpCmdResponse.newResponse(false, "Execute command error, message is " + t.getMessage());
        } finally {
            if (response != null && out != null) {
                PrintWriter writer = new PrintWriter(out);
                writer.print(JSON.toJSONString(response));
                writer.close();
                out.flush();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private HttpCmdRequest parseRequest() throws Exception {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.out = new PrintWriter(socket.getOutputStream());

        String line = in.readLine();
        HttpCmdRequest request = HttpCmdRequest.parse(line);
        out.print("HTTP/1.1 200 OK\r\n\r\n");
        out.flush();
        return request;
    }
}
