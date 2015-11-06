package com.lts.remoting.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class JavaSerializable implements RemotingSerializable {

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public byte[] serialize(Object obj) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {

            oos.writeObject(obj);

            oos.flush();

            return bos.toByteArray();

        } finally {
            oos.close();
            bos.close();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bin);

        try {

            Object obj = ois.readObject();

            return (T) obj;

        } finally {
            ois.close();
            bin.close();
        }
    }

}
