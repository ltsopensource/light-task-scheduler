package com.github.ltsopensource.kv.data;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayOutputStream;
import com.github.ltsopensource.core.json.TypeReference;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.kv.CapacityNotEnoughException;
import com.github.ltsopensource.kv.DB;
import com.github.ltsopensource.kv.DBException;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.index.IndexItem;
import com.github.ltsopensource.kv.serializer.StoreSerializer;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 存放在磁盘
 *
 * @author Robert HG (254963746@qq.com) on 12/14/15.
 */
public class DataBlockEngine<K, V> {

    private static final Logger LOGGER = DB.LOGGER;

    private final ConcurrentMap<Long/*fileId*/, DataBlock> NAME_BLOCK_MAP = new ConcurrentHashMap<Long, DataBlock>();
    private CopyOnWriteArrayList<DataBlock> writableBlocks = new CopyOnWriteArrayList<DataBlock>();
    private CopyOnWriteArrayList<DataBlock> readonlyBlocks = new CopyOnWriteArrayList<DataBlock>();

    private StoreSerializer serializer;
    private File dataPath;
    private StoreConfig storeConfig;
    private ReentrantLock lock = new ReentrantLock();
    private DataCompactor dataCompactor;

    public DataBlockEngine(StoreSerializer serializer, StoreConfig storeConfig) {
        this.serializer = serializer;
        this.storeConfig = storeConfig;
        this.dataPath = storeConfig.getDataPath();
    }

    /**
     * 初始化,从磁盘中加载老的文件
     */
    public void init() throws IOException {
        try {
            FileUtils.createDirIfNotExist(dataPath);
        } catch (IOException e) {
            LOGGER.error("create dataPath " + dataPath + " error:" + e.getMessage(), e);
            throw e;
        }

        String[] dataFiles = dataPath.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(DataBlock.FILE_SUFFIX);
            }
        });

        if (dataFiles.length == 0) {
            return;
        }

        // 得到最大的事务日志id, 看是否需要重放
        StoreTxLogPosition maxTxLog = null;

        for (String dataFile : dataFiles) {
            try {
                DataBlock dataBlock = new DataBlock(dataFile, storeConfig);
                NAME_BLOCK_MAP.put(dataBlock.getFileId(), dataBlock);
                if (dataBlock.isFull()) {
                    readonlyBlocks.add(dataBlock);
                } else {
                    writableBlocks.add(dataBlock);
                }

                if (maxTxLog == null || maxTxLog.getRecordId() < dataBlock.getLastTxLogPosition().getRecordId()) {
                    maxTxLog = dataBlock.getLastTxLogPosition();
                }

            } catch (IOException e) {
                LOGGER.error("load data block [" + dataFile + "] error:" + e.getMessage(), e);
            }
        }
        storeConfig.setLastTxLogPositionOnDataBlock(maxTxLog);
    }

    protected List<DataBlock> getReadonlyBlocks() {
        return readonlyBlocks;
    }

    /**
     * 追加一个键值对
     */
    public DataAppendResult append(StoreTxLogPosition storeTxLogPosition, K key, V value) {

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        try {
            DataEntry<K, V> dataEntry = new DataEntry<K, V>(key, value);
            serializer.serialize(dataEntry, out);

            return append(storeTxLogPosition, out.toByteArray());

        } catch (Exception e) {
            throw new DBException("Persistent data error: " + e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    public V getValue(IndexItem<K> index) {
        try {
            DataBlock dataBlock = NAME_BLOCK_MAP.get(index.getFileId());
            if (dataBlock == null) {
                return null;
            }
            byte[] data = dataBlock.readData(index.getFromIndex(), index.getLength());

            UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(data);
            DataEntry<K, V> dataEntry = serializer.deserialize(is, new TypeReference<DataEntry<K, V>>() {
            }.getType());

            return dataEntry.getValue();

        } catch (Exception e) {
            throw new DBException("Read data error: " + e.getMessage(), e);
        }
    }

    /**
     * 删除某个键值对(逻辑删除),物理删除滞后
     */
    public void remove(StoreTxLogPosition storeTxLogPosition, IndexItem<K> index) {
        DataBlock dataBlock = NAME_BLOCK_MAP.get(index.getFileId());
        if (dataBlock == null) {
            return;
        }
        dataBlock.removeData(storeTxLogPosition, index.getFromIndex(), index.getLength());
    }

    /**
     * 获取一个可以写的block,如果没有则创建一个新的
     */
    private DataBlock getWriteDataBlock() throws IOException {
        if (writableBlocks.size() != 0) {
            return writableBlocks.get(0);
        }

        lock.lock();
        try {
            if (writableBlocks.size() != 0) {
                return writableBlocks.get(0);
            }
            DataBlock dataBlock = new DataBlock(storeConfig);
            NAME_BLOCK_MAP.put(dataBlock.getFileId(), dataBlock);
            writableBlocks.add(dataBlock);
            return dataBlock;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 写数据
     */
    private DataAppendResult append(StoreTxLogPosition storeTxLogPosition, byte[] dataBytes) throws IOException {
        DataBlock writeBlock = getWriteDataBlock();

        try {
            return writeBlock.append(storeTxLogPosition, dataBytes);
        } catch (CapacityNotEnoughException e) {
            if (!readonlyBlocks.contains(writeBlock)) {
                readonlyBlocks.add(writeBlock);
            }
            writableBlocks.remove(writeBlock);

            return append(storeTxLogPosition, dataBytes);
        }
    }

}
