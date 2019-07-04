package com.github.ltsopensource.jobtracker.sender;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobSender {


    public SendResult send(String taskTrackerNodeGroup, String taskTrackerIdentity, int size);


    public static class SendResult {
        private boolean success;
        private Object returnValue;

        public SendResult(boolean success, Object returnValue) {
            this.success = success;
            this.returnValue = returnValue;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }
    }

}
