package com.github.ltsopensource.kv.data;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.io.UnsafeByteArrayInputStream;
import com.github.ltsopensource.kv.CapacityNotEnoughException;
import com.github.ltsopensource.kv.DB;
import com.github.ltsopensource.kv.DBException;
import com.github.ltsopensource.kv.StoreConfig;
import com.github.ltsopensource.kv.txlog.StoreTxLogPosition;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.remoting.common.ServiceThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * 每个数据块格式
 * 1. MAGIC_NUMBER          1 byte
 * 2. data length           4 byte
 * 3. data bytes
 *
 * @author Robert HG (254963746@qq.com) on 12/14/15.
 */
public class DataBlock {

    private final static Logger LOGGER = DB.LOGGER;

    private File file;
    private long fileId;
    private String fileName;
    // 文件大小
    private long fileSize;
    private FileChannel fileChannel;

    private DataBlockFileHeader fileHeader;

    private final long maxDataEntrySize = 1000;
    private StoreConfig storeConfig;

    public static final String FILE_SUFFIX = ".ltsdata";

    private StoreTxLogPosition lastTxLogPosition;

    private FlushDataService flushDataService;

    public DataBlock(String fileName, StoreConfig storeConfig) throws IOException {
        this.fileName = fileName;
        this.fileId = Long.parseLong(fileName.substring(0, fileName.lastIndexOf(".")));
        this.file = new File(storeConfig.getDataPath(), fileName);
        this.storeConfig = storeConfig;
        this.fileSize = storeConfig.getDataBlockFileSize();

        this.fileHeader = new DataBlockFileHeader();
        this.flushDataService = new FlushDataService();

        init();
    }

    public DataBlock(StoreConfig storeConfig) throws IOException {
        this(System.currentTimeMillis() + FILE_SUFFIX, storeConfig);
    }

    private void init() throws IOException {

        boolean success = false;
        try {
            boolean newFile;

            // 判断文件是否存在
            if (this.file.exists()) {
                if (!this.file.isFile()) {
                    throw new IOException(file + " is not a file");
                }
                newFile = false;
            } else {
                newFile = true;
                FileUtils.createFileIfNotExist(file);
            }

            this.fileChannel = FileUtils.newFileChannel(this.file, "rw");

            if (newFile) {
                // 写入文件头部
                fileHeader.write(fileChannel);
                // 新文件写入位置就为头部位置
                fileHeader.setFileLength(fileHeader.getLength());
            } else {
                // 读取文件头部
                fileHeader.read(fileChannel);
                if (fileHeader.getFileLength() == 0) {
                    fileHeader.setFileLength(fileHeader.getLength());
                }
            }
            lastTxLogPosition = new StoreTxLogPosition(fileHeader.getStoreTxLogRecordId());


            flushDataService.start();

            success = true;
        } catch (FileNotFoundException e) {
            LOGGER.error("create file channel " + fileName + " error ", e);
            throw e;
        } catch (IOException e) {
            LOGGER.error("map file " + fileName + " error ", e);
            throw e;
        } finally {
            if (!success) {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            }
        }
    }

    /**
     * 写data
     */
    public DataAppendResult append(StoreTxLogPosition txLog, byte[] dataBytes) throws IOException {

        int length = dataBytes.length;

        DataAppendResult result = new DataAppendResult();

        synchronized (this) {

            if (length > maxDataEntrySize) {
                throw new DBException("Value size can not great than " + maxDataEntrySize);
            }
            if (fileHeader.getFileLength() + length >= fileSize) {
                fileHeader.markFull();
                throw new CapacityNotEnoughException();
            }

            ReadableByteChannel src = Channels.newChannel(new UnsafeByteArrayInputStream(dataBytes));

            long position = fileHeader.getFileLength();

            fileChannel.transferFrom(src, position, length);

            result.setFileId(fileId);
            result.setFromIndex(position);
            result.setLength(length);

            fileHeader.setFileLength(fileHeader.getFileLength() + length);

            fileHeader.getTotalNum().incrementAndGet();
            fileHeader.getAliveNum().incrementAndGet();

            this.lastTxLogPosition = txLog;
        }

        return result;
    }

    public boolean isFull() {
        return fileHeader.isFull();
    }

    public StoreTxLogPosition getLastTxLogPosition() {
        return new StoreTxLogPosition(fileHeader.getStoreTxLogRecordId());
    }

    public void removeData(StoreTxLogPosition txLogPosition, long fromIndex, int length) {
        // 减少存活数就可以了, 物理删除滞后
        fileHeader.getAliveNum().decrementAndGet();
        synchronized (this) {
            this.lastTxLogPosition = txLogPosition;
        }
    }

    public long getFileId() {
        return fileId;
    }

    /**
     * 读取指定位置数据
     */
    public byte[] readData(long fromIndex, int length) throws IOException {

        fileChannel.position(fromIndex);

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        fileChannel.read(byteBuffer);
        return byteBuffer.array();
    }

    private int totalNum;
    private int aliveNum;

    public void flushDisk() throws IOException {

        if (totalNum == fileHeader.getTotalNum().get()
                && aliveNum == fileHeader.getAliveNum().get()) {
            return;
        }

        totalNum = fileHeader.getTotalNum().get();
        aliveNum = fileHeader.getAliveNum().get();

        if (DataBlock.LOGGER.isDebugEnabled()) {
            DataBlock.LOGGER.debug("flush Data start");
        }

        synchronized (this) {
            fileHeader.setStoreTxLogRecordId(lastTxLogPosition.getRecordId());
            // 先把data数据刷到磁盘
            fileChannel.force(true);
        }
        // 再写文件头部汇总信息
        fileHeader.write(fileChannel);

        if (DataBlock.LOGGER.isDebugEnabled()) {
            DataBlock.LOGGER.debug("flush Data end:" + JSON.toJSONString(fileHeader));
        }
    }

    private class FlushDataService extends ServiceThread {

        @Override
        public String getServiceName() {
            return FlushDataService.class.getSimpleName();
        }

        @Override
        public void run() {

            DataBlock.LOGGER.info(this.getServiceName() + " service started");

            while (!this.isStopped()) {
                try {
                    if (storeConfig.isEnableFlushDataInterval()) {
                        Thread.sleep(storeConfig.getFlushDataInterval());
                    } else {
                        waitForRunning(storeConfig.getFlushDataInterval());
                    }
                    // 刷到磁盘
                    flushDisk();
                } catch (Exception e) {
                    DataBlock.LOGGER.error(this.getServiceName() + " error:" + e.getMessage(), e);
                }
            }
        }
    }
}
