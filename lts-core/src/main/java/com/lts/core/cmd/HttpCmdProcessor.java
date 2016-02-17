package com.lts.core.cmd;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public interface HttpCmdProcessor {

    String getCommand();

    HttpCmdResponse execute(HttpCmdRequest request) throws Exception;

}
