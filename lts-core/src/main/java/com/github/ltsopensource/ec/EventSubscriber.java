package com.github.ltsopensource.ec;

/**
 * 事件订阅者
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class EventSubscriber {

    public EventSubscriber(String id, Observer observer) {
        this.id = id;
        this.observer = observer;
    }

    private String id;

    private Observer observer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Observer getObserver() {
        return observer;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }
}
