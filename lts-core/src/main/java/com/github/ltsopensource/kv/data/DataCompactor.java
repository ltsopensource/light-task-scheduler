package com.github.ltsopensource.kv.data;

import com.github.ltsopensource.kv.DB;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.index.Index;
import com.github.ltsopensource.core.logger.Logger;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO 负责将两个block合并成一个,合并的过程中会物理删除已经删除的数据, 而且要更新索引
 *
 * @author Robert HG (254963746@qq.com) on 12/17/15.
 */
public class DataCompactor<K, V> {

    private static final Logger LOGGER = DB.LOGGER;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> future;
    private StoreConfig storeConfig;
    private Index<K, V> index;
    private DataBlockEngine<K, V> dataBlockEngine;
    private AtomicBoolean running = new AtomicBoolean(false);

    public void init() {
        future = this.executorService.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!running.compareAndSet(false, true)) {
                            return;
                        }
                        try {

                            compact();
                        } catch (Throwable t) {
                            LOGGER.error("DataCompactor compactor error:" + t.getMessage(), t);
                        } finally {
                            running.set(false);
                        }
                    }
                }, storeConfig.getDataBlockCompactCheckInterval(),
                storeConfig.getDataBlockCompactCheckInterval(), TimeUnit.MILLISECONDS);
    }

    private void compact() {
        // 未完成的compact文件

        // 检查是否有需要合并的block, 容量小于50%
        List<DataBlock> readonlyBlocks = dataBlockEngine.getReadonlyBlocks();


        // 合并这两个block

    }

}
