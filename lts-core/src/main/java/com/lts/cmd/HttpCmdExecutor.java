package com.lts.cmd;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

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

            if (request == null) {
                response = HttpCmdResponse.newResponse(false, "Request Error");
                return;
            }

            if (StringUtils.isEmpty(request.getCommand())) {
                response = HttpCmdResponse.newResponse(false, "Command is blank");
                return;
            }

            if (StringUtils.isEmpty(request.getNodeIdentity())) {
                response = HttpCmdResponse.newResponse(false, "nodeIdentity is blank");
                return;
            }

            HttpCmdProc httpCmdProc = context.getCmdProcessor(request.getNodeIdentity(), request.getCommand());

            if (httpCmdProc != null) {
                response = httpCmdProc.execute(request);
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

        HttpCmdRequest request = null;
        if (line.startsWith("GET")) {
            request = parseGet(line);
        } else if (line.startsWith("POST")) {        // 当做POST处理
            request = parsePost(in, line);
        } else {
            out.print("HTTP/1.1 405 Method Not Allowed\r\n\r\n");
            out.flush();
            return null;
        }
        out.print("HTTP/1.1 200 OK\r\n\r\n");
        out.flush();
        return request;
    }

    private HttpCmdRequest parsePost(BufferedReader in, String line) throws Exception {
        HttpRequest httpRequest = new HttpRequest();

        final String firstLine = line;
        char[] postData;
        while (true) {
            if (line == null || line.equals("")) {
                int contentLength = Integer.parseInt(httpRequest.headers.get("Content-Length"));
                if (contentLength > 0) {
                    postData = new char[contentLength];
                    in.read(postData);
                    httpRequest.parsePost(new String(postData));
                }
                break;
            }
            httpRequest.parseRequestLine(line);
            line = in.readLine();
        }

        HttpCmdRequest request = parseGet(firstLine);
        for (Map.Entry<String, String> entry : httpRequest.postVars.entrySet()) {
            request.addParam(entry.getKey(), entry.getValue());
        }
        return request;
    }

    /**
     * GET /nodeIdentity/xxxCommand?xxx=yyyyy HTTP/1.1
     */
    protected static HttpCmdRequest parseGet(String url) throws Exception {

        HttpCmdRequest request = new HttpCmdRequest();

        if (StringUtils.isEmpty(url)) {
            return request;
        }
        int start = url.indexOf('/');
        int ask = url.indexOf('?') == -1 ? url.lastIndexOf(' ') : url.indexOf('?');
        int space = url.lastIndexOf(' ');
        String path = url.substring(start != -1 ? start + 1 : 0, ask != -1 ? ask : url.length());
        String nodeIdentity = path.substring(0, path.indexOf('/'));
        String command = path.substring(path.indexOf('/') + 1, path.length());
        request.setCommand(command);
        request.setNodeIdentity(nodeIdentity);

        if (ask == -1 || ask == space) {
            return request;
        }

        String paramStr = url.substring(ask + 1, space != -1 ? space : url.length());

        for (String param : paramStr.split("&")) {
            if (StringUtils.isEmpty(param)) {
                continue;
            }
            String[] kvPair = param.split("=");
            if (kvPair.length != 2) {
                continue;
            }

            String key = StringUtils.trim(kvPair[0]);
            String value = StringUtils.trim(kvPair[1]);
            value = URLDecoder.decode(value, "UTF-8");

            request.addParam(key, value);
        }
        return request;
    }

    private static class HttpRequest {

        protected String method = null;
        protected String url = null;
        protected String protocol = null;

        protected HashMap<String, String> headers = new HashMap<String, String>();
        protected HashMap<String, String> postVars = new HashMap<String, String>();

        protected String parseError = null;

        public void parseRequestLine(String line) {
            if (line == null) {
                parseError();
            } else if (!line.contains(":")) {
                String[] lineParts = line.split(" ");
                method = lineParts[0];
                if (lineParts[1] != null)
                    url = lineParts[1];
                if (lineParts[2] != null) protocol = lineParts[2];
            } else {
                String[] parts = line.split(": ");
                headers.put(parts[0], parts[1]);
            }
        }

        public void parsePost(String post) throws Exception {
            String[] postData = post.split("&");
            for (String aPostData : postData) {
                String[] postPair = aPostData.split("=");
                String key = postPair[0];
                String value = postPair[1];
                if(value != null){
                    value = URLDecoder.decode(value, "UTF-8");
                }
                postVars.put(key, value);
            }
        }

        public String parseError() {
            return this.parseError;
        }
    }
}
