package com.lts.job.core.compiler;

import com.lts.job.core.extension.SPI;

/**
 * Compiler. (SPI, Singleton, ThreadSafe)
 * @author william.liangf
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
