package com.lts.core.compiler;

import com.lts.core.extension.SPI;

/**
 * Compiler. (SPI, Singleton, ThreadSafe)
 */
@SPI("javassist")
public interface Compiler {

    /**
     * Compile java source code.
     *
     * @param code        Java source code
     * @param classLoader
     * @return Compiled class
     */
    Class<?> compile(String code, ClassLoader classLoader);

}
