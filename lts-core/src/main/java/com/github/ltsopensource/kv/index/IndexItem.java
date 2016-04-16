package com.github.ltsopensource.kv.index;

import java.io.Serializable;

/**
 * Key 在内存中的index
 *
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class IndexItem<K> implements Serializable {

    /**
     * key
     */
    private K key;

    /**
     * 所在文件
     */
    private long fileId;
    /**
     * 在文件的起始位置
     */
    private long fromIndex;

    /**
     * 写的内容的长度
     */
    private int length;

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(long fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexItem<?> indexItem = (IndexItem<?>) o;

        if (fileId != indexItem.fileId) return false;
        if (fromIndex != indexItem.fromIndex) return false;
        if (length != indexItem.length) return false;
        return key != null ? key.equals(indexItem.key) : indexItem.key == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (int) (fileId ^ (fileId >>> 32));
        result = 31 * result + (int) (fromIndex ^ (fromIndex >>> 32));
        result = 31 * result + length;
        return result;
    }

    @Override
    public String toString() {
        return "IndexItem{" +
                "key=" + key +
                ", fileId=" + fileId +
                ", fromIndex=" + fromIndex +
                ", length=" + length +
                '}';
    }
}
