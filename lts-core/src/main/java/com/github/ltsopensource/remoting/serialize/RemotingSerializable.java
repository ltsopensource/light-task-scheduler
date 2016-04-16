package com.github.ltsopensource.remoting.serialize;


import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI(key = SpiExtensionKey.REMOTING_SERIALIZABLE_DFT, dftValue = "fastjson")
public interface RemotingSerializable {

    int getId();

    byte[] serialize(final Object obj) throws Exception;

    <T> T deserialize(final byte[] data, Class<T> clazz) throws Exception;
}
