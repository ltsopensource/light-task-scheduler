package com.lts.core.commons.file;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 文件锁
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class FileLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLock.class);
    private File file;

    private FileChannel channel = null;
    private java.nio.channels.FileLock fileLock = null;
    // 保证一个jvm同时只有一个线程获得文件锁
    private Semaphore semaphore;
    // 防止一个JVM 多个 实例访问同一个文件锁
    private static final Map<String/*文件名*/, Semaphore> LOCK_SEMAPHORE = new HashMap<String, Semaphore>();

    public FileLock(String filename) {
        file = new File(filename);
        FileUtils.createFileIfNotExist(file.getAbsolutePath());
        synchronized (LOCK_SEMAPHORE) {
            semaphore = LOCK_SEMAPHORE.get(filename);
            if (semaphore == null) {
                semaphore = new Semaphore(1);
                LOCK_SEMAPHORE.put(filename, semaphore);
            }
        }
    }

    /**
     * 获得锁
     */
    public boolean tryLock(long timeout) {
        try {
            boolean acquire = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            if (!acquire) {
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }

        try {
            Path path = Paths.get(file.getPath());
            if (channel != null && channel.isOpen()) {
                return false;
            }
            channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ);
            fileLock = channel.tryLock();
            if (fileLock != null) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void release() {
        try {
            if (channel != null) {
                try {
                    channel.close();   // also releases the lock
                } catch (IOException e) {
                    LOGGER.error(" file channel close failed.", e);
                }
                fileLock = null;
            }
        } finally {
            semaphore.release();
        }
    }
}
