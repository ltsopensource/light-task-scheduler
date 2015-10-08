package com.lts.core.extension.factory;

import com.lts.core.extension.ExtensionFactory;
import com.lts.core.extension.ExtensionLoader;
import com.lts.core.extension.SPI;

/**
 * SpiExtensionFactory
 */
public class SpiExtensionFactory implements ExtensionFactory {

    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            if (loader.getSupportedExtensions().size() > 0) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }

}
