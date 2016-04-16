package com.github.ltsopensource.remoting.serialize;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class FastJsonSerializable implements RemotingSerializable {

    @Override
    public int getId() {
        return 1;
    }

    public byte[] serialize(Object obj) throws Exception {
        String json = toJson(obj, false);
        return json.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        final String json = new String(data, Charset.forName("UTF-8"));
        return fromJson(json, clazz);
    }

    private String toJson(final Object obj, boolean prettyFormat) {
        return JSON.toJSONString(obj, prettyFormat);
    }

    private <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

}
