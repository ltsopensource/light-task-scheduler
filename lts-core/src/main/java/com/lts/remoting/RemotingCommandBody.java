package com.lts.remoting;

import com.lts.remoting.exception.RemotingCommandFieldCheckException;

import java.io.Serializable;

/**
 * RemotingCommand中自定义字段反射对象的公共接口
 */
public interface RemotingCommandBody extends Serializable{

    public void checkFields() throws RemotingCommandFieldCheckException;
}
