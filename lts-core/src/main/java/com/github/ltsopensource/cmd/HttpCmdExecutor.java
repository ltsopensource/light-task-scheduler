package com.github.ltsopensource.cmd;

import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class HttpCmdExecutor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCmdExecutor.class);
    private HttpCmdContext context;
    private Socket socket;

    public HttpCmdExecutor(HttpCmdContext context, Socket socket) {
        this.context = context;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            // 解析请求
            HttpCmdRequest request = parseRequest();

            Assert.notNull(request, "Request Error");

            Assert.hasText(request.getCommand(), "Command is blank");

            Assert.hasText(request.getNodeIdentity(), "nodeIdentity is blank");

            HttpCmdProc httpCmdProc = context.getCmdProcessor(request.getNodeIdentity(), request.getCommand());

            Assert.notNull(httpCmdProc, "Can not find the command:[" + request.getCommand() + "]");

            sendResponse(HTTP_OK, JSON.toJSONString(httpCmdProc.execute(request)));

        } catch (HttpCMDErrorException ignored) {
            // 忽略
        } catch (IllegalArgumentException e) {
            sendError(HTTP_BADREQUEST, JSON.toJSONString(HttpCmdResponse.newResponse(false, e.getMessage())), false);
        } catch (Throwable t) {
            LOGGER.error("Error When Execute Command", t);
            sendError(HTTP_INTERNALERROR, JSON.toJSONString(HttpCmdResponse.newResponse(false, "Error:" + t.getMessage())), false);
        }
    }

    private HttpCmdRequest parseRequest() throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

        StringTokenizer st = new StringTokenizer(in.readLine());
        if (!st.hasMoreTokens())
            sendError(HTTP_BADREQUEST, "BAD REQUEST: Syntax error");

        String method = st.nextToken();

        if (!st.hasMoreTokens())
            sendError(HTTP_BADREQUEST, "BAD REQUEST: Missing URI");

        String uri = st.nextToken();

        Properties params = new Properties();
        assert uri != null;
        int qmi = uri.indexOf('?');
        if (qmi >= 0) {
            decodeParams(uri.substring(qmi + 1), params);
            uri = uri.substring(0, qmi);
        }

        Properties header = new Properties();
        if (st.hasMoreTokens()) {
            String line = in.readLine();
            while (line.trim().length() > 0) {
                int p = line.indexOf(':');
                header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                line = in.readLine();
            }
        }

        if (method.equalsIgnoreCase("POST")) {
            long size = 0x7FFFFFFFFFFFFFFFL;
            String contentLength = header.getProperty("Content-Length");
            if (contentLength == null) {
                contentLength = header.getProperty("content-length");
            }
            if (contentLength != null) {
                size = Integer.parseInt(contentLength);
            }
            String postLine = "";
            char buf[] = new char[512];
            int read = in.read(buf);
            while (read >= 0 && size > 0 && !postLine.endsWith("\r\n")) {
                size -= read;
                postLine += String.valueOf(buf, 0, read);
                if (size > 0)
                    read = in.read(buf);
            }
            postLine = postLine.trim();
            decodeParams(postLine, params);
        }
        return resolveRequest(uri, params);
    }


    protected static HttpCmdRequest resolveRequest(String uri, Properties params) {

        HttpCmdRequest request = new HttpCmdRequest();
        String[] pathNode = uri.substring(1, uri.length()).split("/");
        String nodeIdentity = pathNode[0];
        String command = pathNode[1];
        ;
        request.setCommand(command);
        request.setNodeIdentity(nodeIdentity);

        for (Map.Entry<Object, Object> entry : params.entrySet()) {
            request.addParam(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return request;
    }

    public static final String HTTP_OK = "200 OK", HTTP_REDIRECT = "301 Moved Permanently",
            HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
            HTTP_BADREQUEST = "400 Bad Request", HTTP_INTERNALERROR = "500 Internal Server Error",
            HTTP_NOTIMPLEMENTED = "501 Not Implemented";

    public static final String MIME_PLAINTEXT = "text/plain", MIME_HTML = "text/html",
            MIME_DEFAULT_BINARY = "application/octet-stream";

    private void sendError(String status, String msg) {
        sendError(status, msg, true);
    }

    private void sendError(String status, String msg, boolean needInterrupt) {
        sendResponse(status, msg);
        if (needInterrupt) {
            throw new HttpCMDErrorException();
        }
    }

    private void sendResponse(String status, String msg) {
        sendResponse(status, MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
    }

    private void decodeParams(String params, Properties p) throws Exception {
        if (params == null)
            return;

        StringTokenizer st = new StringTokenizer(params, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                String key = e.substring(0, sep);
                String value = URLDecoder.decode((e.substring(sep + 1)), "UTF-8");
                p.put(key, value);
            }
        }
    }

    private void sendResponse(String status, String mime, Properties header, InputStream data) {
        try {
            if (status == null)
                throw new Error("sendResponse(): Status can't be null.");
            OutputStream out = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(out);
            pw.print("HTTP/1.0 " + status + " \r\n");

            if (mime != null)
                pw.print("Content-Type: " + mime + "\r\n");

            if (header == null || header.getProperty("Date") == null)
                pw.print("Date: " + DateUtils.formatYMD_HMS(new Date()) + "\r\n");

            if (header != null) {
                Enumeration e = header.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String value = header.getProperty(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            pw.print("\r\n");
            pw.flush();

            if (data != null) {
                byte[] buff = new byte[2048];
                while (true) {
                    int read = data.read(buff, 0, 2048);
                    if (read <= 0)
                        break;
                    out.write(buff, 0, read);
                }
            }
            out.flush();
            out.close();
            if (data != null)
                data.close();
        } catch (IOException ioe) {
            try {
                socket.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private class HttpCMDErrorException extends RuntimeException {
        public HttpCMDErrorException() {
            super();
        }
    }
}
