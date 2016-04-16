package com.github.ltsopensource.zookeeper.serializer;


import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayOutputStream;

import java.io.*;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class SerializableSerializer implements ZkSerializer {
    @Override
    public byte[] serialize(Object serializable) throws ZkMarshallingException {
        try {
            if (serializable == null) {
                return null;
            }
            UnsafeByteArrayOutputStream byteArrayOS = new UnsafeByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
            stream.writeObject(serializable);
            stream.close();
            return byteArrayOS.toByteArray();
        } catch (IOException e) {
            throw new ZkMarshallingException(e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingException {
        if (bytes == null) {
            return null;
        }
        try {
            return new ObjectInputStream(new UnsafeByteArrayInputStream(bytes)).readObject();
        } catch (ClassNotFoundException e) {
            throw new ZkMarshallingException("Unable to find object class.", e);
        } catch (IOException e) {
            throw new ZkMarshallingException(e);
        }
    }
}
