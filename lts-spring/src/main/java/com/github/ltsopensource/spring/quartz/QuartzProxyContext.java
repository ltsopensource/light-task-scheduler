package com.github.ltsopensource.spring.quartz;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzProxyContext {

    private QuartzLTSConfig quartzLTSConfig;
    private QuartzLTSProxyAgent agent;

    public QuartzProxyContext(QuartzLTSConfig quartzLTSConfig, QuartzLTSProxyAgent agent) {
        this.quartzLTSConfig = quartzLTSConfig;
        this.agent = agent;
    }

    public QuartzLTSConfig getQuartzLTSConfig() {
        return quartzLTSConfig;
    }

    public QuartzLTSProxyAgent getAgent() {
        return agent;
    }
}
