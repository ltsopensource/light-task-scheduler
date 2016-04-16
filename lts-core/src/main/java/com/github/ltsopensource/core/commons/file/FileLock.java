package com.github.ltsopensource.core.commons.file;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * 文件锁
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public class FileLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLock.class);

    private FileChannel channel = null;
    private java.nio.channels.FileLock lock = null;
    private RandomAccessFile randomAccessFile;

    public FileLock(String filename) {
        this(new File(filename));
    }

    public FileLock(File file) {
        FileUtils.createFileIfNotExist(file);
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得锁
     */
    public boolean tryLock() {
        boolean success = false;
        try {
            if (channel != null && channel.isOpen()) {
                return false;
            }
            channel = randomAccessFile.getChannel();
            lock = channel.tryLock();
            if (lock != null) {
                success = true;
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (!success) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void release() {
        try {
            if (lock != null) {
                lock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                try {
                    channel.close();   // also releases the lock
                } catch (IOException e) {
                    LOGGER.error("file channel close failed.", e);
                }
            }
        }
    }
}
