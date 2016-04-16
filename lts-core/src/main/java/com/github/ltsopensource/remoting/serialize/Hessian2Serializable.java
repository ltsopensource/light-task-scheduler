package com.github.ltsopensource.remoting.serialize;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayOutputStream;

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

        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        out.startMessage();
        out.writeObject(obj);
        out.completeMessage();
        out.close();
        return bos.toByteArray();
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {

        UnsafeByteArrayInputStream bin = new UnsafeByteArrayInputStream(data);
        Hessian2Input in = new Hessian2Input(bin);
        in.startMessage();
        Object obj = in.readObject(clazz);
        in.completeMessage();
        in.close();
        return (T) obj;
    }

}
