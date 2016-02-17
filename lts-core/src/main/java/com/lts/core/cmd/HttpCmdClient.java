package com.lts.core.cmd;

import com.lts.core.commons.utils.CollectionUtils;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class HttpCmdClient {

    /**
     * 执行命令, 内部也就是一个http请求
     */
    public static <Resp extends HttpCmdResponse> Resp execute(String ip, int port, HttpCmd<Resp> cmd) {

        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(ip).append(":").append(port).append("/").append(cmd.getCommand());

        try {
            Map<String, String> params = cmd.getParams();
            if (CollectionUtils.isNotEmpty(params)) {
                String prefix = "?";
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(prefix);
                    prefix = "&";
                    sb.append(String.format("%s=%s", entry.getKey(),
                            URLEncoder.encode(entry.getValue(), "UTF-8")));
                }
            }
            return cmd.reqSend(sb.toString());
        } catch (Exception e) {
            throw new HttpCmdException(e);
        }
    }

}
