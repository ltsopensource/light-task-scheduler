package com.github.ltsopensource.cmd;

/**
 * Cmd 处理器
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public interface HttpCmdProc {

    String nodeIdentity();

    String getCommand();

    HttpCmdResponse execute(HttpCmdRequest request) throws Exception;

}
