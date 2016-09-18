package com.github.ltsopensource.kv.serializer;


import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.json.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * JSON 序列化
 * Robert HG (254963746@qq.com) 12/5/15.
 */
public class JsonStoreSerializer implements StoreSerializer {

    @Override
    public void serialize(Object value, OutputStream out) throws IOException {
        String v = JSON.toJSONString(value);
        if (v != null) {
            out.write(v.getBytes("UTF-8"));
        }
    }

    @Override
    public <T> T deserialize(InputStream in, Type type) throws IOException {
        String v = FileUtils.read(in, "UTF-8");
        return JSON.parse(v, type);
    }
}
