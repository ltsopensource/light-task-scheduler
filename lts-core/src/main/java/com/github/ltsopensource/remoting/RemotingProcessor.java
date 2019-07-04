package com.github.ltsopensource.remoting;

/**
 * 接收请求处理器，服务器与客户端通用
 */
public interface RemotingProcessor {

    String processRequest(String chanel, String request);
}
