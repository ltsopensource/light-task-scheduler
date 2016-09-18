package com.github.ltsopensource.remoting.protocol;

public final class RemotingProtos {
    private RemotingProtos() {
    }

    public enum ResponseCode {
        // 成功
        SUCCESS(0),
        // 发生了未捕获异常
        SYSTEM_ERROR(1),
        // 由于线程池拥堵，系统繁忙
        SYSTEM_BUSY(2),
        // 请求代码不支持
        REQUEST_CODE_NOT_SUPPORTED(3),
        // 请求参数错误
        REQUEST_PARAM_ERROR(4);

        private int code;

        ResponseCode(int code) {
            this.code = code;
        }

        public static ResponseCode valueOf(int code) {
            for (ResponseCode responseCode : ResponseCode.values()) {
                if (responseCode.code == code) {
                    return responseCode;
                }
            }
            throw new IllegalArgumentException("can't find the response code !");
        }

        public int code() {
            return this.code;
        }


    }
}
