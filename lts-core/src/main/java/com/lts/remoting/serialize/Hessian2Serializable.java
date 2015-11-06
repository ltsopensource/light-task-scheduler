package com.lts.remoting.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class Hessian2Serializable implements RemotingSerializable {

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public byte[] serialize(Object obj) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Hessian2Output out = new Hessian2Output(bos);

        out.startMessage();

        out.writeObject(obj);

        out.completeMessage();
        out.close();
        bos.close();

        return bos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Hessian2Input in = new Hessian2Input(bin);

        in.startMessage();

        Object obj = in.readObject();

        in.completeMessage();
        in.close();
        bin.close();

        return (T) obj;
    }

}
