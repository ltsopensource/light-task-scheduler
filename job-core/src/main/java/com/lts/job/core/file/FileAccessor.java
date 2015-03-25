package com.lts.job.core.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         文件访问器 (多进程多线程互斥)
 */
public class FileAccessor {

    // 分隔符 (用来区分两个对象)
    private static final String SEPARATOR = "\r\n";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAccessor.class);
    private File file;

    RandomAccessFile randomAccessFile = null;
    FileChannel channel = null;
    FileLock lock = null;

    public FileAccessor(String filename) throws FileException {
        file = new File(filename);
    }

    /**
     * 文件的最后修改时间
     *
     * @return
     */
    public long lastModified() {
        return file.lastModified();
    }

    /**
     * 删除文件
     */
    public void delete() {
        try {
            file.delete();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean exists() {
        return file.exists();
    }

    public void createIfNotExist() {
        FileUtils.createFileIfNotExist(file.getAbsolutePath());
    }

    /**
     * 创建文件访问器
     *
     * @param filename
     * @return
     * @throws FileException
     */
    public static FileAccessor create(String filename) throws FileException {
        FileAccessor fileAccessor = new FileAccessor(filename);
        fileAccessor.createIfNotExist();
        return fileAccessor;
    }

    /**
     * 向文件中追加一行内容
     *
     * @param line
     */
    public void addOneLine(Line line) throws FileException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            fileWriter.write(line.toString() + SEPARATOR);
        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_ADD);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 一次性添加多行
     *
     * @param lines
     * @throws FileException
     */
    public void addLines(List<Line> lines) throws FileException {
        if (lines != null && lines.size() > 0) {
            for (Line line : lines) {
                addOneLine(line);
            }
        }
    }

    /**
     * 读取所有内容
     *
     * @return List<一行的字符串>
     */
    public List<Line> readLines() throws FileException {

        List<Line> lines = new ArrayList<Line>();
        try {
            tryLock();

            BufferedReader reader = new BufferedReader(new FileReader(randomAccessFile.getFD()));
            // 读取所有内容

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals("")) {
                    lines.add(new Line(line));
                }
            }
        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_GET);
        } finally {
            unlock();
        }
        return lines;
    }

    /**
     * 删除文件的前lines 行
     *
     * @param num
     */
    public void deleteFirstLines(int num) throws FileException {

        List<Line> lines = new ArrayList<Line>();
        try {
            tryLock();

            BufferedReader reader = new BufferedReader(new FileReader(randomAccessFile.getFD()));
            // 读取所有内容

            int lineNum = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (++lineNum > num) {
                    lines.add(new Line(line));
                }
            }
            channel.truncate(0);

            if (lines != null && lines.size() > 0) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(randomAccessFile.getFD()));
                for (Line line1 : lines) {
                    writer.write(line1.toString() + SEPARATOR);
                }
                writer.flush();
            }

        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_GET);
        } finally {
            unlock();
        }
    }

    /**
     * 清空文件
     */
    public void empty() throws FileException {
        try {
            tryLock();

            channel.truncate(0);

        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_EMPTY);
        } finally {
            unlock();
        }
    }

    /**
     * 清空文件
     * 如果当前 修改时间和给定相同，那么情况
     */
    public boolean compareAndEmpty(Long lastModified) throws FileException {
        try {
            tryLock();

            if (lastModified == lastModified()) {
                channel.truncate(0);
                return true;
            }
            return false;

        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_EMPTY);
        } finally {
            unlock();
        }
    }

    /**
     * 判断文件是否为空
     *
     * @throws FileException
     */
    public boolean isEmpty() throws FileException {
        try {
            tryLock();

            return randomAccessFile.length() == 0;

        } catch (Exception e) {
            throw new FileException(e, FileException.FILE_CONTENT_EMPTY);
        } finally {
            unlock();
        }
    }

    /**
     * 获得锁
     */
    public void tryLock() {
        while (true) {
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
                channel = randomAccessFile.getChannel();

                lock = channel.tryLock();
                if (lock == null) {
                    throw new AccessDeniedException("can not get file lock!");
                }
                // 获得到锁, 跳出
                break;
            } catch (IOException e) {
                LOGGER.warn("can not get file lock! other jvm or thread hold the lock, and sleep 50 ms .");
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
        }
    }

    /**
     * 关闭文件
     */
    public void unlock() {

        if (lock != null) {
            try {
                lock.release();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
