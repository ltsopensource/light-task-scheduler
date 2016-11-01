package com.github.ltsopensource.kv.index;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayOutputStream;
import com.github.ltsopensource.core.json.TypeReference;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.replay.TxLogReplay;
import com.github.ltsopensource.kv.serializer.StoreSerializer;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class MemIndexSnapshot<K, V> extends AbstractIndexSnapshot<K, V> {

    private TxLogReplay<K, V> txLogReplay;
    private AtomicBoolean snapshoting = new AtomicBoolean(false);

    public MemIndexSnapshot(TxLogReplay<K, V> txLogReplay, Index<K, V> index, StoreConfig storeConfig, StoreSerializer serializer) {
        super(index, storeConfig, serializer);
        this.txLogReplay = txLogReplay;
    }

    @Override
    protected void loadFromDisk() throws IOException {

        FileUtils.createDirIfNotExist(storeConfig.getIndexPath());

        String[] indexFiles = getIndexFiles();
        if (indexFiles == null || indexFiles.length == 0) {
            return;
        }

        FileChannel fileChannel = null;
        try {
            File lastSnapshot = new File(storeConfig.getIndexPath(), indexFiles[indexFiles.length - 1]);
            fileChannel = FileUtils.newFileChannel(lastSnapshot, "rw");
            IndexSnapshotFileHeader fileHeader = new IndexSnapshotFileHeader();
            fileHeader.read(fileChannel);

            ConcurrentMap<K, IndexItem<K>> indexMap = null;
            if (fileHeader.getStoreTxLogRecordId() != 0) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Start to read IndexSnapshot File ....");
                }

                UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream();
                WritableByteChannel target = Channels.newChannel(os);
                long readLength = fileChannel.size() - fileHeader.getLength();
                if (readLength != 0) {
                    fileChannel.transferTo(fileHeader.getLength(), readLength, target);

                    UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(os.toByteArray());

                    indexMap = serializer.deserialize(is,
                            new TypeReference<ConcurrentSkipListMap<K, IndexItem<K>>>() {
                            }.getType());
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Finish read IndexSnapshot File");
                }
            }

            if (indexMap == null) {
                indexMap = new ConcurrentSkipListMap<K, IndexItem<K>>();
            }

            ((MemIndex<K, V>) index).setIndexMap(indexMap);

            StoreTxLogPosition lastTxLog = new StoreTxLogPosition();
            lastTxLog.setRecordId(fileHeader.getStoreTxLogRecordId());

            ((MemIndex<K, V>) index).setLastTxLog(lastTxLog);

        } finally {
            if (fileChannel != null) {
                fileChannel.close();
            }
        }
    }

    private String[] getIndexFiles() throws IOException {
        String[] indexFiles = storeConfig.getIndexPath().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".snapshot");
            }
        });

        if (indexFiles == null) {
            throw new IOException("can't list file in " + storeConfig.getIndexPath());
        }

        if (indexFiles.length == 0) {
            return null;
        }

        Arrays.sort(indexFiles, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                return left.compareTo(right);
            }
        });

        return indexFiles;
    }

    @Override
    protected void replayTxLog() {
        // 重放
        StoreTxLogPosition indexTxLog = index.lastTxLog();
        StoreTxLogPosition dataTxLog = storeConfig.getLastTxLogPositionOnDataBlock();

        // 需要重放的位置
        StoreTxLogPosition replayTxLog = null;
        if (dataTxLog == null) {
            replayTxLog = indexTxLog;
        } else if (indexTxLog == null) {
            replayTxLog = dataTxLog;
        } else {
            replayTxLog = (indexTxLog.getRecordId() < dataTxLog.getRecordId() ? indexTxLog : dataTxLog);
        }

        if (replayTxLog == null) {
            return;
        }

        txLogReplay.replay(replayTxLog);
    }

    private StoreTxLogPosition lastStoreTxLogPosition;

    @Override
    public void snapshot() throws IOException {
        if (!snapshoting.compareAndSet(false, true)) {
            return;
        }
        try {
            StoreTxLogPosition storeTxLogPosition = index.lastTxLog();

            if (storeTxLogPosition == null) {
                return;
            }
            if (lastStoreTxLogPosition != null && lastStoreTxLogPosition.getRecordId() == storeTxLogPosition.getRecordId()) {
                return;
            }

            ConcurrentMap<K, IndexItem<K>> indexMap = ((MemIndex<K, V>) index).getIndexMap();

            String name = System.currentTimeMillis() + ".snapshot";
            File snapshot = new File(storeConfig.getIndexPath(), name);
            FileChannel fileChannel = FileUtils.newFileChannel(snapshot, "rw");

            IndexSnapshotFileHeader fileHeader = new IndexSnapshotFileHeader();
            UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream();
            try {
                serializer.serialize(indexMap, os);
                byte[] payload = os.toByteArray();
                ReadableByteChannel src = Channels.newChannel(new UnsafeByteArrayInputStream(payload));

                // 先写一个空的文件头
                fileHeader.write(fileChannel);

                // 写内容
                fileChannel.transferFrom(src, fileHeader.getLength(), payload.length);
            } finally {
                os.close();
            }

            fileChannel.force(true);

            // 写真实的文件头
            fileHeader.setStoreTxLogRecordId(storeTxLogPosition.getRecordId());
            fileHeader.write(fileChannel);

            // 删除多余的快照数目
            deleteOverSnapshot();

            LOGGER.info("snapshot index finished: [" + name + "]");

            lastStoreTxLogPosition = storeTxLogPosition;
        } finally {
            snapshoting.set(false);
        }
    }

    /**
     * 删除多余的快照数目
     */
    private void deleteOverSnapshot() throws IOException {
        String[] indexFiles = getIndexFiles();
        if (indexFiles == null || indexFiles.length == 0) {
            return;
        }

        if (storeConfig.getMaxIndexSnapshotSize() > 1 && indexFiles.length > storeConfig.getMaxIndexSnapshotSize()) {

            for (int i = 0; i < indexFiles.length - storeConfig.getMaxIndexSnapshotSize(); i++) {

                FileUtils.delete(new File(storeConfig.getIndexPath(), indexFiles[i]));
                LOGGER.info("delete index snapshot [" + indexFiles[i] + "] succeed");
            }

        }

    }
}
