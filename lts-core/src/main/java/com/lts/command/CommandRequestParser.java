package com.lts.command;

import com.lts.core.commons.utils.StringUtils;

import java.net.URLDecoder;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class CommandRequestParser {

    /**
     * GET /xxxCommand?xxx=yyyyy HTTP/1.1
     */
    protected static CommandRequest parse(String url) throws Exception {

        CommandRequest request = new CommandRequest();

        if (StringUtils.isEmpty(url)) {
            return request;
        }
        int start = url.indexOf('/');
        int ask = url.indexOf('?') == -1 ? url.lastIndexOf(' ') : url.indexOf('?');
        int space = url.lastIndexOf(' ');
        String target = url.substring(start != -1 ? start + 1 : 0, ask != -1 ? ask : url.length());
        request.setCommand(target);
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

}
