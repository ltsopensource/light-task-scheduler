package com.github.ltsopensource.core.protocol;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 *         用于定义通信协议中的一些code
 */
public class JobProtos {

    private JobProtos() {
    }

    public enum RequestCode {
        // 心跳
        HEART_BEAT(10),
        // 提交任务
        SUBMIT_JOB(11),
        // 询问 任务执行的情况
        ASK_JOB_PROGRESS(12),
        // 分发任务
        PUSH_JOB(13),
        // 任务执行完成
        JOB_COMPLETED(14),
        // 询问任务
        JOB_ASK(15),
        // 请求推送任务
        JOB_PULL(16),
        // TaskTracker的业务日志
        BIZ_LOG_SEND(17),
        // 取消(删除)任务
        CANCEL_JOB(18),
        // 提交依赖任务
        SUBMIT_DEP_JOB_GROUP(19),
        ;

        private int code;

        RequestCode(int code) {
            this.code = code;
        }

        public static RequestCode valueOf(int code) {
            for (RequestCode requestCode : RequestCode.values()) {
                if (requestCode.code == code) {
                    return requestCode;
                }
            }
            throw new IllegalArgumentException("can't find the request code !");
        }

        public int code() {
            return this.code;
        }
    }

    public enum ResponseCode {
        // 任务执行中
        JOB_IN_PROGRESS(10),
        // 接受任务成功
        JOB_RECEIVE_SUCCESS(11),
        // 接收任务失败
        JOB_RECEIVE_FAILED(12),
        // 任务执行失败
        JOB_RUN_FAILURE(13),
        // 没有任务节点执行
        NO_TASK_TRACKER(15),
        // 心跳成功
        HEART_BEAT_SUCCESS(16),
        // 没有节点分组
        NO_NODE_GROUP(17),
        // 没有可用的 任务执行
        NO_AVAILABLE_JOB_RUNNER(18),
        // 任务推送成功
        JOB_PUSH_SUCCESS(19),
        // 任务处理成功
        JOB_NOTIFY_SUCCESS(20),
        // 任务推送
        JOB_PULL_SUCCESS(21),
        // 业务日志发送成功
        BIZ_LOG_SEND_SUCCESS(22),
        // 任务删除成功
        JOB_CANCEL_SUCCESS(23),
        // 任务删除失败
        JOB_CANCEL_FAILED(24),
        // 任务执行错误
        JOB_RUN_ERROR(25),;


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
