package com.github.ltsopensource.kv.txlog;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayOutputStream;
import com.github.ltsopensource.core.json.TypeReference;
import com.github.ltsopensource.kv.*;
import com.github.ltsopensource.kv.serializer.StoreSerializer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class StoreTxLogEngine<K, V> {

    private volatile StoreTxLog storeTxLog;
    private StoreSerializer serializer;
    private AtomicBoolean initialed = new AtomicBoolean(false);
    private List<StoreTxLog> storeTxLogs;
    private StoreConfig storeConfig;
    private static final String LOG_FILE_SUFFIX = StoreTxLog.LOG_FILE_SUFFIX;

    // Log 目录
    private File logPath;

    public StoreTxLogEngine(StoreSerializer serializer, StoreConfig storeConfig) {
        this.logPath = storeConfig.getLogPath();
        this.storeConfig = storeConfig;
        this.serializer = serializer;
        this.storeTxLogs = new CopyOnWriteArrayList<StoreTxLog>();
    }

    public void init() throws IOException {
        if (!initialed.compareAndSet(false, true)) {
            return;
        }

        // 从path里面读取老的Log文件
        FileUtils.createDirIfNotExist(logPath);

        String[] logFiles = logPath.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(LOG_FILE_SUFFIX);
            }
        });

        if (logFiles == null) {
            throw new IOException("can't list file in " + logPath);
        }

        if (logFiles.length > 0) {

            Arrays.sort(logFiles, new Comparator<String>() {
                @Override
                public int compare(String left, String right) {
                    return left.compareTo(right);
                }
            });

            for (int i = 0; i < logFiles.length; i++) {
                String logFile = logFiles[i];
                boolean isWritable = ++i == logFiles.length;
                // 只有最后一个文件才是可写的
                StoreTxLog storeTxLog = new StoreTxLog(
                        storeConfig, new File(logPath, logFile), !isWritable, false, 0);
                if (i > 1) {
                    storeTxLogs.get(i - 1).setNext(storeTxLog);
                }
                storeTxLogs.add(storeTxLog);

                if (isWritable) {
                    this.storeTxLog = storeTxLog;
                }
            }
        } else {
            // 新建一个文件
            String name = System.currentTimeMillis() + LOG_FILE_SUFFIX;
            this.storeTxLog = new StoreTxLog(storeConfig, new File(logPath, name), false, true, 0);
            storeTxLogs.add(storeTxLog);
        }
    }

    private StoreTxLog nextNewStoreTxLog() throws IOException {
        long firstRecordId = storeTxLog.getNextRecordId();
        String name = System.currentTimeMillis() + LOG_FILE_SUFFIX;
        StoreTxLog newStoreTxLog = new StoreTxLog(storeConfig, new File(logPath, name), false, true, firstRecordId);
        storeTxLogs.add(newStoreTxLog);
        storeTxLog.setNext(newStoreTxLog);
        storeTxLog = newStoreTxLog;
        return storeTxLog;
    }

    /**
     * 追加一条事务日志
     */
    public StoreTxLogPosition append(Operation op, K key) throws DBException {
        return this.append(op, key, null);
    }

    /**
     * 追加一条事务日志
     */
    public StoreTxLogPosition append(Operation op, K key, V value) throws DBException {
        try {
            try {
                return this.append(storeTxLog, op, key, value);
            } catch (CapacityNotEnoughException notEnough) {
                // 要新建一个文件
                return this.append(nextNewStoreTxLog(), op, key, value);
            }
        } catch (Exception e) {
            throw new DBException("append dbLog error:" + e.getMessage(), e);
        }
    }

    private StoreTxLogPosition append(StoreTxLog storeTxLog, Operation op, K key, V value) throws IOException {
        StoreTxLogEntry<K, V> entry = null;
        long timestamp = System.currentTimeMillis();
        switch (op) {
            case PUT:
                entry = new StoreTxLogEntry<K, V>(op, key, value, timestamp);
                break;
            case REMOVE:
                entry = new StoreTxLogEntry<K, V>(op, key, timestamp);
                break;
        }

        UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream();
        try {
            serializer.serialize(entry, out);
            byte[] entryBytes = out.toByteArray();
            return storeTxLog.append(entryBytes);
        } finally {
            out.close();
        }
    }

    public Cursor<StoreTxLogCursorEntry<K, V>> cursor(StoreTxLogPosition position) {

        long recordId = position.getRecordId();
        if (storeTxLogs.size() == 0) {
            return new EmptyCursor<StoreTxLogCursorEntry<K, V>>();
        }
        StoreTxLog targetTxLog = null;
        for (StoreTxLog txLog : storeTxLogs) {
            if (recordId >= txLog.getFirstRecordId() && recordId < txLog.getNextRecordId()) {
                targetTxLog = txLog;
                break;
            }
        }

        if (targetTxLog == null) {
            return new EmptyCursor<StoreTxLogCursorEntry<K, V>>();
        }

        return new StoreTxLogCursor(targetTxLog, recordId - targetTxLog.getFirstRecordId());
    }

    private class StoreTxLogCursor implements Cursor<StoreTxLogCursorEntry<K, V>> {

        private StoreTxLog currentTxLog;
        private long position;

        public StoreTxLogCursor(StoreTxLog currentTxLog, long position) {
            this.currentTxLog = currentTxLog;
            if (position <= 0) {
                position = currentTxLog.getHeaderLength();
            }
            this.position = position;
        }

        @Override
        public boolean hasNext() {
            // 1. 判断该文件是否还有内容
            if (currentTxLog == null) {
                return false;
            }
            if (position < currentTxLog.getFileLength()) {
                return true;
            }

            // 2. 如果该文件没有内容了,判断是否还有下一个文件
            if (currentTxLog.next() == null) {
                return false;
            }

            currentTxLog = currentTxLog.next();
            position = currentTxLog.getHeaderLength();
            return true;
        }

        @Override
        public StoreTxLogCursorEntry<K, V> next() {
            try {
                byte[] entry = currentTxLog.readEntry(position);
                int entryLength = entry.length;
                UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(entry);
                StoreTxLogEntry<K, V> storeTxLogEntry = serializer.deserialize(is, new TypeReference<StoreTxLogEntry<K, V>>() {
                }.getType());

                StoreTxLogCursorEntry<K, V> storeTxLogCursorEntry = new StoreTxLogCursorEntry<K, V>();
                storeTxLogCursorEntry.setStoreTxLogEntry(storeTxLogEntry);
                storeTxLogCursorEntry.setPosition(new StoreTxLogPosition(currentTxLog.getFirstRecordId() + position));

                this.position = currentTxLog.nextEntryPosition(position, entryLength);

                return storeTxLogCursorEntry;

            } catch (IOException e) {
                throw new DBException("Cursor next() error:" + e.getMessage(), e);
            }
        }
    }

}
