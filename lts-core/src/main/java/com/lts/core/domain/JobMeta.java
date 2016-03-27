package com.lts.core.domain;

import com.lts.core.json.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/13/15.
 */
public class JobMeta implements Serializable {

    private static final long serialVersionUID = 1476984243004969158L;

    private String jobId;
    private Map<String, String> internalExtParams;

    private Job job;

    public JobMeta() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Map<String, String> getInternalExtParams() {
        return internalExtParams;
    }

    public void setInternalExtParams(Map<String, String> internalExtParams) {
        this.internalExtParams = internalExtParams;
    }

    public String getInternalExtParam(String key) {
        if (internalExtParams == null) {
            return null;
        }
        return internalExtParams.get(key);
    }

    public void setInternalExtParam(String key, String value) {
        if (internalExtParams == null) {
            internalExtParams = new HashMap<String, String>();
        }
        internalExtParams.put(key, value);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
