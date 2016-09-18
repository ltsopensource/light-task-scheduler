package com.github.ltsopensource.kv.index;

import com.github.ltsopensource.kv.AbstractFileHeader;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 文件头部
 * 1. magic                  2 byte
 * 3. storeTxLogPosition     8 byte      // 快照到的最后一条事务日志ID
 * <p/>
 *
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class IndexSnapshotFileHeader extends AbstractFileHeader {

    private final static short magic = (short) 0xBE03;
    // 8 byte       (最后的一个txLog的记录ID)
    private long storeTxLogRecordId;

    @Override
    public int getLength() {
        return 2 + 8;
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

        this.storeTxLogRecordId = byteBuffer().getLong();
    }

    @Override
    public void write(FileChannel fileChannel) throws IOException {

        byteBuffer().position(0);
        byteBuffer().putShort(magic);
        byteBuffer().putLong(storeTxLogRecordId);
        byteBuffer().flip();

        fileChannel.position(0);
        fileChannel.write(byteBuffer());
        fileChannel.force(true);
    }

    public long getStoreTxLogRecordId() {
        return storeTxLogRecordId;
    }

    public void setStoreTxLogRecordId(long storeTxLogRecordId) {
        this.storeTxLogRecordId = storeTxLogRecordId;
    }
}
