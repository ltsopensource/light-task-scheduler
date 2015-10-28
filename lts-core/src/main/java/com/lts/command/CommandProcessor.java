package com.lts.command;

import java.io.OutputStream;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public interface CommandProcessor {

    public void execute(OutputStream out, CommandRequest request) throws Exception;

}
