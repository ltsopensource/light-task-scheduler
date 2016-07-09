package com.github.ltsopensource.kv;

import com.github.ltsopensource.kv.index.IndexType;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;

import java.io.File;

/**
 * @author Robert HG (254963746@qq.com) on 12/15/15.
 */
public class StoreConfig {

    // 数据库目录
    private File dbPath;
    // 数据存放目录
    private File dataPath;
    // 事务日志存放目录
    private File logPath;
    // 索引目录
    private File indexPath;
    // 索引snapshot的间隔
    private int indexSnapshotInterval = 60 * 1000;     // 60s
    private int indexSnapshotThreshold = 1000;         // 当超过一千个改变的时候snapshot
    // 定时刷盘时间
    private int flushDataInterval = 1000;   // 1s
    // 是否开启定时刷盘, 默认不开启
    private boolean enableFlushDataInterval = true;
    // 每个数据库的大小 512M
    private int dataBlockFileSize = 512 * 1024 * 1024;
    // 每个log文件的大小 1G
    private int txLogFileSize = 1024 * 1024 * 1024;
    // 每个txLog Entry的最大长度
    private int maxxLogEntryLength = 1024;
    // dblog 定时刷盘时间
    private int dbLogFlushInterval = 1000;
    // 索引的引擎类型
    private IndexType indexType = IndexType.MEM;
    // 最大的data缓存大小
    private int maxDataCacheSize = 1000;
    // 启动时候读取出来的最后一个事务日志ID
    private StoreTxLogPosition lastTxLogPositionOnDataBlock;
    // 最大的索引快照数
    private int maxIndexSnapshotSize = 3;
    // DataBlock 合并检查间隔
    private int dataBlockCompactCheckInterval = 60 * 1000;
    // index每次批量写入的size是100条
    private int indexSnapshotBatchSize = 100;

    public int getDbLogFlushInterval() {
        return dbLogFlushInterval;
    }

    public void setDbLogFlushInterval(int dbLogFlushInterval) {
        this.dbLogFlushInterval = dbLogFlushInterval;
    }

    public int getFlushDataInterval() {
        return flushDataInterval;
    }

    public void setFlushDataInterval(int flushDataInterval) {
        this.flushDataInterval = flushDataInterval;
    }

    public boolean isEnableFlushDataInterval() {
        return enableFlushDataInterval;
    }

    public void setEnableFlushDataInterval(boolean enableFlushDataInterval) {
        this.enableFlushDataInterval = enableFlushDataInterval;
    }

    public int getDataBlockFileSize() {
        return dataBlockFileSize;
    }

    public void setDataBlockFileSize(int dataBlockFileSize) {
        this.dataBlockFileSize = dataBlockFileSize;
    }

    public int getTxLogFileSize() {
        return txLogFileSize;
    }

    public void setTxLogFileSize(int txLogFileSize) {
        this.txLogFileSize = txLogFileSize;
    }

    public File getDbPath() {
        return dbPath;
    }

    public void setDbPath(File dbPath) {
        this.dbPath = dbPath;
    }

    public File getDataPath() {
        return dataPath;
    }

    public void setDataPath(File dataPath) {
        this.dataPath = dataPath;
    }

    public File getLogPath() {
        return logPath;
    }

    public void setLogPath(File logPath) {
        this.logPath = logPath;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public int getMaxDataCacheSize() {
        return maxDataCacheSize;
    }

    public void setMaxDataCacheSize(int maxDataCacheSize) {
        this.maxDataCacheSize = maxDataCacheSize;
    }

    public int getIndexSnapshotInterval() {
        return indexSnapshotInterval;
    }

    public void setIndexSnapshotInterval(int indexSnapshotInterval) {
        this.indexSnapshotInterval = indexSnapshotInterval;
    }

    public File getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(File indexPath) {
        this.indexPath = indexPath;
    }

    public StoreTxLogPosition getLastTxLogPositionOnDataBlock() {
        return lastTxLogPositionOnDataBlock;
    }

    public void setLastTxLogPositionOnDataBlock(StoreTxLogPosition lastTxLogPositionOnDataBlock) {
        this.lastTxLogPositionOnDataBlock = lastTxLogPositionOnDataBlock;
    }

    public int getMaxxLogEntryLength() {
        return maxxLogEntryLength;
    }

    public void setMaxxLogEntryLength(int maxxLogEntryLength) {
        this.maxxLogEntryLength = maxxLogEntryLength;
    }

    public int getMaxIndexSnapshotSize() {
        return maxIndexSnapshotSize;
    }

    public void setMaxIndexSnapshotSize(int maxIndexSnapshotSize) {
        this.maxIndexSnapshotSize = maxIndexSnapshotSize;
    }

    public int getDataBlockCompactCheckInterval() {
        return dataBlockCompactCheckInterval;
    }

    public void setDataBlockCompactCheckInterval(int dataBlockCompactCheckInterval) {
        this.dataBlockCompactCheckInterval = dataBlockCompactCheckInterval;
    }

    public int getIndexSnapshotThreshold() {
        return indexSnapshotThreshold;
    }

    public void setIndexSnapshotThreshold(int indexSnapshotThreshold) {
        this.indexSnapshotThreshold = indexSnapshotThreshold;
    }

    public int getIndexSnapshotBatchSize() {
        return indexSnapshotBatchSize;
    }

    public void setIndexSnapshotBatchSize(int indexSnapshotBatchSize) {
        this.indexSnapshotBatchSize = indexSnapshotBatchSize;
    }
}
