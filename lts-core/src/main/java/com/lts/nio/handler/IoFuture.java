package com.lts.nio.handler;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.remoting.Future;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class IoFuture implements Future {

    private static final Logger LOGGER = LoggerFactory.getLogger(IoFuture.class);
    private boolean success = false;
    private Throwable cause;
    private String msg;
    private List<IoFutureListener> listeners;
    private boolean completed = false;

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Throwable cause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void addListener(IoFutureListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<IoFutureListener>();
        }
        listeners.add(listener);
        if (completed) {
            complete(listener);
        }
    }

    public void addListener(List<IoFutureListener> listeners) {
        if (listeners != null) {
            if (this.listeners == null) {
                this.listeners = new ArrayList<IoFutureListener>();
            }
            this.listeners.addAll(listeners);
        }
        if (completed) {
            for (IoFutureListener listener : listeners) {
                complete(listener);
            }
        }
    }

    public void removeListener(IoFutureListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    public void notifyListeners() {
        completed = true;
        if (this.listeners != null) {
            for (IoFutureListener ioFutureListener : listeners) {
                complete(ioFutureListener);
            }
        }
    }

    private void complete(IoFutureListener listener) {
        try {
            listener.operationComplete(this);
        } catch (Exception e) {
            LOGGER.error("notify listener {} error ", listener, e);
        }
    }
}
