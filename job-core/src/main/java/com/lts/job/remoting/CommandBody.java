package com.lts.job.remoting;


import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;

/**
 * RemotingCommand中自定义字段反射对象的公共接口
 */
public interface CommandBody {

    public void checkFields() throws RemotingCommandFieldCheckException;
}
