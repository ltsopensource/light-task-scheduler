package com.github.ltsopensource.kv.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Robert HG (254963746@qq.com) 12/5/15.
 */
public interface StoreSerializer {

    void serialize(Object value, OutputStream out) throws IOException;

    <T> T deserialize(InputStream in, Type type) throws IOException;
}
