package com.lts.core.command;

import java.io.OutputStream;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public interface HttpCommandProcessor {

    public void execute(OutputStream out, HttpCommandRequest request) throws Exception;

}
