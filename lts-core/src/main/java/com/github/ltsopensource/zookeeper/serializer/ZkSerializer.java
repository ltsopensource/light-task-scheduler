package com.github.ltsopensource.zookeeper.serializer;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public interface ZkSerializer {

    public byte[] serialize(Object data) throws ZkMarshallingException;

    public Object deserialize(byte[] bytes) throws ZkMarshallingException;
}
