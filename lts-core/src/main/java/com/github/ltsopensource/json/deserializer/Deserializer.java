package com.github.ltsopensource.json.deserializer;

import java.lang.reflect.Type;

/**
 * @author Robert HG (254963746@qq.com) on 12/30/15.
 */
public interface Deserializer {

    <T> T deserialize(Object object, Type type);

}
