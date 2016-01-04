package com.lts.core.command;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCommandClient {

    public static void sendCommand(String ip, int port, HttpCommand httpCommand) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(ip).append(":").append(port).append("/").append(httpCommand.getCommand());

        Map<String, String> params = httpCommand.getParams();
        if (params != null) {
            sb.append("?");
            boolean isFirst = true;
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (!isFirst) {
                    sb.append("&");
                } else {
                    isFirst = false;
                }
                sb.append(e.getKey()).append("=").append(e.getValue());
            }
        }

        URL url = new URL(sb.toString());
        InputStream inputStream = url.openStream();
        httpCommand.proceedRequest(inputStream);
    }

}
