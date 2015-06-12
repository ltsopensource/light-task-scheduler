package com.lts.remoting;

import com.lts.remoting.exception.RemotingCommandFieldCheckException;

/**
 * RemotingCommand中自定义字段反射对象的公共接口
 */
public interface CommandBody {

    public void checkFields() throws RemotingCommandFieldCheckException;
}
