package com.lts.job.core.support;

import com.lts.job.core.file.FileAccessor;
import com.lts.job.core.file.FileException;
import com.lts.job.core.file.Line;
import com.lts.job.core.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 * 重试定时器 (用来发送 给 客户端的反馈信息等)
 */
public abstract class RetryScheduler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

    private Class<T> clazz = GenericsUtils.getSuperClassGenericType(this.getClass());

    // 定时检查是否有 师表的反馈任务信息(给客户端的)
    private ScheduledExecutorService RETRY_EXECUTOR_SERVICE;
    private FileAccessor fileAccessor;
    // 文件锁 (同一时间只能有一个线程在 检查提交失败的任务)
    private com.lts.job.core.file.FileAccessor fileLockAccessor;

    // 批量发送的消息数
    private int batchSize = 5;

    public RetryScheduler() {
        try {
            fileAccessor = com.lts.job.core.file.FileAccessor.create(Application.Config.getFilePath());
            fileLockAccessor = new FileAccessor(Application.Config.getFilePath() + ".lock");
        } catch (FileException e) {
            LOGGER.error(" Get file accessor error!", e);
        }
    }

    protected RetryScheduler(int batchSize) {
        this();
        this.batchSize = batchSize;
    }

    public void start() {
        if (fileAccessor != null) {
            RETRY_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
            // 这个时间后面再去优化
            RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new CheckRunner(), 30, 30, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        RETRY_EXECUTOR_SERVICE.shutdown();
        RETRY_EXECUTOR_SERVICE = null;
    }

    /**
     * 定时检查 提交失败任务的Runnable
     */
    private class CheckRunner implements Runnable {

        @Override
        public void run() {
            try {

                if (fileLockAccessor.exists()) {
                    // 有其他线程或者进程在访问
                    return;
                }

                fileLockAccessor.create();

                // 1. 检测 远程连接 是否可用
                if (!isRemotingEnable()) {
                    return;
                }

                if (!fileAccessor.isEmpty()) {
                    long lastModified = fileAccessor.lastModified();
                    List<Line> lines = fileAccessor.readLines();

                    if (lines != null && lines.size() > 0) {

                        int segments = lines.size() / batchSize;

                        for (int i = 0; i <= segments; i++) {
                            List<Line> subLines = BatchUtils.getBatchList(i, batchSize, lines);
                            if (!CollectionUtils.isEmpty(subLines)) {
                                List<T> list = new ArrayList<T>(subLines.size());
                                for (Line line : subLines) {
                                    list.add((T) JsonUtils.jsonToObject(line.toString(), clazz));
                                }
                                if (retry(list)) {
                                    // 只有1页
                                    if (segments == 0) {
                                        // 如果没有被修改过, 直接清空
                                        if (!fileAccessor.compareAndEmpty(lastModified)) {
                                            fileAccessor.deleteFirstLines(lines.size());
                                        }
                                    } else {
                                        fileAccessor.deleteFirstLines(lines.size());
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                fileLockAccessor.delete();
            }
        }
    }

    /**
     * 远程连接是否可用
     *
     * @return
     */
    protected abstract boolean isRemotingEnable();

    /**
     * 重试
     *
     * @param list
     * @return
     */
    protected abstract boolean retry(List<T> list);

    public FileAccessor getFileAccessor() {
        return fileAccessor;
    }
}
