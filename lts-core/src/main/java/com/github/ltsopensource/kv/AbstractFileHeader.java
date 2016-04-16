package com.github.ltsopensource.kv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Robert HG (254963746@qq.com) on 12/17/15.
 */
public abstract class AbstractFileHeader {

    private ByteBuffer byteBuffer;

    public AbstractFileHeader() {
        this.byteBuffer = ByteBuffer.allocate(getLength());
    }

    /**
     * 得到文件头buffer
     */
    public ByteBuffer byteBuffer() {
        return this.byteBuffer;
    }

    /**
     * 文件头长度
     */
    public abstract int getLength();

    /**
     * 从FileChannel中读取内容
     */
    public abstract void read(FileChannel fileChannel) throws IOException;

    /**
     * 将文件头内容写入到文件头部
     */
    public abstract void write(FileChannel fileChannel) throws IOException;
}
