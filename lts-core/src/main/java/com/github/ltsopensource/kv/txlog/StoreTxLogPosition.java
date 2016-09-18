package com.github.ltsopensource.kv.txlog;

import java.io.Serializable;

/**
 * 事务日志结果
 *
 * @author Robert HG (254963746@qq.com) on 12/16/15.
 */
public class StoreTxLogPosition implements Serializable {

    // 写的记录id  = 文件的第一条记录ID + 写的位置
    private long recordId;

    public StoreTxLogPosition() {
    }

    public StoreTxLogPosition(long recordId) {
        this.recordId = recordId;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }
}
