package com.github.ltsopensource.kv.data;

import com.github.ltsopensource.kv.AbstractFileHeader;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件头部
 * 1. magic             2 byte
 * 2. fileLength            4 byte      // 文件的长度
 * 3. totalNum              4 byte      // 总的记录数
 * 4. aliveNum              4 byte      // 存活的记录数
 * 5. isFull                4 byte      // 是否满了 1:满了 0 没满
 * 6. storeTxLogPosition    8 byte      // 刷盘到的最后一条事务日志ID
 * <p/>
 *
 * @author Robert HG (254963746@qq.com) on 12/17/15.
 */
public class DataBlockFileHeader extends AbstractFileHeader {

    // 2 byte
    private final static short magic = (short) 0xBE01;
    // 8 byte       (文件长度)
    private long fileLength;
    // 4 byte       (这个Block总的记录个数)
    private AtomicInteger totalNum = new AtomicInteger(0);
    // 4 byte       (这个Block存活的记录个数)
    private AtomicInteger aliveNum = new AtomicInteger(0);
    // 4 byte       (0:没满 , 1:满了)
    private int isFull;
    // 8 byte       (最后的一个txLog的记录ID)
    private long storeTxLogRecordId;

    @Override
    public int getLength() {
        return 2 + 8 + 4 + 4 + 4 + 8;
    }


    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public AtomicInteger getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum.set(totalNum);
    }

    public AtomicInteger getAliveNum() {
        return aliveNum;
    }

    public void setAliveNum(int aliveNum) {
        this.aliveNum.set(aliveNum);
    }

    public boolean isFull() {
        return isFull == 1;
    }

    public void markFull() {
        this.isFull = 1;
    }

    public int getIsFull() {
        return isFull;
    }

    public void setIsFull(int isFull) {
        this.isFull = isFull;
    }

    public long getStoreTxLogRecordId() {
        return storeTxLogRecordId;
    }

    public void setStoreTxLogRecordId(long storeTxLogRecordId) {
        this.storeTxLogRecordId = storeTxLogRecordId;
    }

    @Override
    public void read(FileChannel fileChannel) throws IOException {
        if (fileChannel.size() == 0) {
            return;
        }
        fileChannel.position(0);
        fileChannel.read(byteBuffer());

        byteBuffer().position(0);
        short readMagic = byteBuffer().getShort();
        if (readMagic != magic) {
            throw new IOException("Invalid file type magic number 0x" + Integer.toHexString(readMagic & 0xFFFF));
        }

        this.fileLength = byteBuffer().getLong();
        this.totalNum.set(byteBuffer().getInt());
        this.aliveNum.set(byteBuffer().getInt());
        this.isFull = byteBuffer().getInt();
        this.storeTxLogRecordId = byteBuffer().getLong();
    }

    @Override
    public void write(FileChannel fileChannel) throws IOException {

        byteBuffer().position(0);
        byteBuffer().putShort(magic);
        byteBuffer().putLong(fileLength);
        byteBuffer().putInt(totalNum.get());
        byteBuffer().putInt(aliveNum.get());
        byteBuffer().putInt(isFull);
        byteBuffer().putLong(storeTxLogRecordId);
        byteBuffer().flip();

        fileChannel.position(0);
        fileChannel.write(byteBuffer());
        fileChannel.force(true);
    }

}
