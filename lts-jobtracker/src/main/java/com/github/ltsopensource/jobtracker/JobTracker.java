package com.github.ltsopensource.jobtracker;

import com.github.ltsopensource.jobtracker.support.OldDataHandler;
import com.github.ltsopensource.remoting.RemotingProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public interface JobTracker {

    void start();

    void stop();

    void beforeStart();

    void afterStart();

    void afterStop();

    void beforeStop();

    RemotingProcessor getDefaultProcessor();

    void setOldDataHandler(OldDataHandler oldDataHandler);

}
