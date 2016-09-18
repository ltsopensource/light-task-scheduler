package com.github.ltsopensource.alarm;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class AlarmMessage implements Serializable {

    private long time;

    private AlarmType type;

    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public AlarmType getType() {
        return type;
    }

    public void setType(AlarmType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AlarmMessage{" +
                "time=" + time +
                ", type=" + type +
                ", msg='" + msg + '\'' +
                '}';
    }
}
