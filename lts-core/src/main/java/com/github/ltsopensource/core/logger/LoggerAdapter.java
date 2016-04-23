package com.github.ltsopensource.core.logger;


import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

import java.io.File;

/**
 * 日志输出器供给器
 */
@SPI(key = ExtConfig.LTS_LOGGER, dftValue = "slf4j")
public interface LoggerAdapter {

    /**
     * 获取日志输出器
     *
     * @param key 分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
    Logger getLogger(Class<?> key);

    /**
     * 获取日志输出器
     *
     * @param key 分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
    Logger getLogger(String key);

    /**
     * 设置输出等级
     *
     * @param level 输出等级
     */
    void setLevel(Level level);

    /**
     * 获取当前日志等级
     *
     * @return 当前日志等级
     */
    Level getLevel();

    /**
     * 获取当前日志文件
     *
     * @return 当前日志文件
     */
    File getFile();

    /**
     * 设置输出日志文件
     *
     * @param file 输出日志文件
     */
    void setFile(File file);

}