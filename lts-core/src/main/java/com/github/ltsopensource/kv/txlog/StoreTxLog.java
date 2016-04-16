package com.github.ltsopensource.kv.txlog;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.kv.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;

/**
 * 记录格式
 * 1.MAGIC_NUMBER   1 byte
 * 2.timestamp      8 byte
 * 3.logEntrySize   4 byte
 * 4.logEntry
 * <p/>
 * 非线程安全
 *
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class StoreTxLog implements Closeable {

    private static final Logger LOGGER = DB.LOGGER;
    private StoreTxLog next;

    private FileChannel fileChannel;
    private StoreConfig storeConfig;

    private final ByteBuffer entryBuffer;
    private StoreTxLogFileHeader fileHeader;

    private static final int ENTRY_HEAD_LENGTH = 1 + 4;
    private static final byte magic = (byte) 0xA2;

    public static final String LOG_FILE_SUFFIX = ".log";

    private long lastCheckPointLength = 0;

    private Timer syncTimer;
    private FutureTimerTask syncTimerTask;
    private FutureTimerTask.Callable syncCallable;

    private long fileLength;

    public StoreTxLog(StoreConfig storeConfig, File file, boolean readonly,
                      boolean isNewFile, long firstRecordId) throws IOException {
        this.storeConfig = storeConfig;
        this.entryBuffer = ByteBuffer.allocate(storeConfig.getMaxxLogEntryLength() + 1 + 4);
        this.fileHeader = new StoreTxLogFileHeader();

        if (!readonly) {
            syncTimer = new Timer("ltsdb-dblog-sync-timer", true);

            syncCallable = new FutureTimerTask.Callable() {
                @Override
                public void call() throws Exception {
                    checkPoint();
                }
            };
        }

        if (isNewFile && file.exists()) {
            throw new IOException(file + " exists already");
        } else {
            FileUtils.createFileIfNotExist(file);
        }

        fileChannel = FileUtils.newFileChannel(file, "rw");

        if (isNewFile) {
            // 新文件长度为头部长度
            fileLength = fileHeader.getLength();
            fileHeader.setFirstRecordId(firstRecordId);

            fileHeader.write(fileChannel);
        } else {
            fileHeader.read(fileChannel);
            fileLength = fileChannel.size();
            lastCheckPointLength = fileLength;
        }
    }

    /**
     * 新添加一条记录
     *
     * @return 返回记录ID
     */
    public StoreTxLogPosition append(byte[] entry) throws IOException {

        int length = entry.length;

        if (length > storeConfig.getMaxxLogEntryLength()) {
            throw new DBException("Value size can not great than " + storeConfig.getMaxxLogEntryLength());
        }

        // 检查当前文件容量是否足够
        if (fileLength + length + ENTRY_HEAD_LENGTH > storeConfig.getTxLogFileSize()) {
            throw new CapacityNotEnoughException();
        }

        StoreTxLogPosition result = new StoreTxLogPosition();
        result.setRecordId(getNextRecordId());

        boolean ok = false;
        try {

            entryBuffer.clear();
            entryBuffer.put(magic);                      // 1 byte
            entryBuffer.putInt(length);                         // 4 byte
            entryBuffer.put(entry);                             // entry.length
            entryBuffer.flip();

            fileChannel.position(fileLength);
            fileChannel.write(entryBuffer);

            int entryLength = (ENTRY_HEAD_LENGTH + length);
            fileLength += entryLength;

            ok = true;
        } finally {
            if (ok) {
                if (syncTimerTask == null || syncTimerTask.isDone()) {
                    syncTimerTask = new FutureTimerTask("ltsdb-dblog-sync-timertask", syncCallable);
                    syncTimer.schedule(syncTimerTask, storeConfig.getDbLogFlushInterval());
                }
            }
        }
        return result;
    }

    public byte[] readEntry(long position) throws IOException {
        fileChannel.position(position);
        entryBuffer.clear();
        fileChannel.read(entryBuffer);

        entryBuffer.position(0);
        byte readMagic = entryBuffer.get();
        if (readMagic != magic) {
            throw new IOException("Invalid entry type magic number 0x" + Integer.toHexString(readMagic & 0xFFFF));
        }
        int length = entryBuffer.getInt();
        byte[] entry = new byte[length];
        entryBuffer.get(entry);
        return entry;
    }

    public long nextEntryPosition(long position, long entryLength) {
        return position + entryLength + 1 + 4;
    }

    public long getFileLength() {
        return fileLength;
    }

    public long getNextRecordId() {
        return fileHeader.getFirstRecordId() + fileLength;
    }

    public long getFirstRecordId() {
        return fileHeader.getFirstRecordId();
    }

    private void checkPoint() throws IOException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("checkpoint start");
        }
        // 先将内容都强制刷到磁盘,因为后面会写头部汇总信息
        if (fileLength != lastCheckPointLength) {
            fileChannel.force(true);
            lastCheckPointLength = fileLength;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("checkpoint end: fileLength=" + fileLength);
        }
    }

    public void setNext(StoreTxLog next) {
        this.next = next;
    }

    public StoreTxLog next() {
        return this.next;
    }

    public int getHeaderLength() {
        return fileHeader.getLength();
    }

    @Override
    public void close() throws IOException {
        checkPoint();
    }

}
