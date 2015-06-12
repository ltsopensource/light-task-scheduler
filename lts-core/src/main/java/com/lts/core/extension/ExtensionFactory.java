package com.lts.core.extension;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
@SPI
public interface ExtensionFactory {

    /**
     * Get extension.
     *
     * @param type object type.
     * @param name object name.
     * @return object instance.
     */
    <T> T getExtension(Class<T> type, String name);

}

