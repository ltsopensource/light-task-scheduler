package com.lts.job.spring;

/**
 * Created by hugui on 3/6/15.
 */
public enum JobClientType {

    NORMAL("normal"),       // 正常的
    RETRY("retry");         // 重试的

    private String value;

    JobClientType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static JobClientType parse(String value) {
        for (JobClientType jobClientType : JobClientType.values()) {
            if (jobClientType.value.equals(value)) {
                return jobClientType;
            }
        }
        throw new IllegalArgumentException("value" + value + "错误");
    }
}
