package com.lts.remoting.serialize;


import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiKey;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI(key = SpiKey.REMOTING_SERIALIZABLE_DFT, dftValue = "fastjson")
public interface RemotingSerializable {

    int getId();

    byte[] serialize(final Object obj) throws Exception;

    <T> T deserialize(final byte[] data, Class<T> clazz) throws Exception;
}
