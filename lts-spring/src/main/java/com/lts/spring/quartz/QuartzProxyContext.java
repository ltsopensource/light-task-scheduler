package com.lts.spring.quartz;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzProxyContext {

    private LTSQuartzConfig ltsQuartzConfig;
    private LTSQuartzProxyAgent agent;

    public QuartzProxyContext(LTSQuartzConfig ltsQuartzConfig, LTSQuartzProxyAgent agent) {
        this.ltsQuartzConfig = ltsQuartzConfig;
        this.agent = agent;
    }

    public LTSQuartzConfig getLtsQuartzConfig() {
        return ltsQuartzConfig;
    }

    public LTSQuartzProxyAgent getAgent() {
        return agent;
    }
}
