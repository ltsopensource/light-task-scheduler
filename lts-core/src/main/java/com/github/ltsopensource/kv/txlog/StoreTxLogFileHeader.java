package com.github.ltsopensource.kv.txlog;

import com.github.ltsopensource.kv.AbstractFileHeader;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 头部 格式
 * 1. magic      2 byte
 * 3. firstRecordId 8 byte
 * <p/>
 *
 * @author Robert HG (254963746@qq.com) on 12/17/15.
 */
public class StoreTxLogFileHeader extends AbstractFileHeader {

    // 2 byte
    private final static short magic = (short) 0xBE02;
    // 8 byte
    private long firstRecordId;

    public long getFirstRecordId() {
        return firstRecordId;
    }

    public void setFirstRecordId(long firstRecordId) {
        this.firstRecordId = firstRecordId;
    }

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
        this.firstRecordId = byteBuffer().getLong();
    }

    @Override
    public void write(FileChannel fileChannel) throws IOException {
        byteBuffer().position(0);
        byteBuffer().putShort(magic);
        byteBuffer().putLong(this.firstRecordId);
        byteBuffer().flip();

        fileChannel.position(0);
        fileChannel.write(byteBuffer());
        fileChannel.force(true);
    }
}
