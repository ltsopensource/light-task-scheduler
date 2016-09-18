package com.github.ltsopensource.kv;

import com.github.ltsopensource.core.logger.Logger;

import java.util.TimerTask;

/**
 * @author Robert HG (254963746@qq.com) on 12/16/15.
 */
public class FutureTimerTask extends TimerTask {

    private final Logger LOGGER = DB.LOGGER;
    private String name;
    private boolean done;
    private Callable callable;

    public FutureTimerTask(String name, Callable callable) {
        this.callable = callable;
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void run() {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error(getName() + " run error:" + e.getMessage(), e);
        } finally {
            done = true;
        }
    }

    public String getName() {
        return name;
    }

    public interface Callable {
        void call() throws Exception;
    }
}
