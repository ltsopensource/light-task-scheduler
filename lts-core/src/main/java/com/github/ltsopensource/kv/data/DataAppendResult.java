package com.github.ltsopensource.kv.data;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 12/15/15.
 */
public class DataAppendResult implements Serializable {

    private long fileId;
    // 写的起始位置
    private long fromIndex;
    // 长度
    private int length;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(long fromIndex) {
        this.fromIndex = fromIndex;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
}
