package com.github.ltsopensource.remoting.codec;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.remoting.serialize.AdaptiveSerializable;
import com.github.ltsopensource.remoting.serialize.RemotingSerializable;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public abstract class AbstractCodec implements Codec {

    protected RemotingSerializable getRemotingSerializable(int serializableTypeId) {

        RemotingSerializable serializable = null;
        if (serializableTypeId > 0) {
            serializable = AdaptiveSerializable.getSerializableById(serializableTypeId);
            if (serializable == null) {
                throw new IllegalArgumentException("Can not support RemotingSerializable that serializableTypeId=" + serializableTypeId);
            }
        } else {
            serializable = ServiceLoader.load(RemotingSerializable.class, Constants.ADAPTIVE);
        }
        return serializable;
    }

}
