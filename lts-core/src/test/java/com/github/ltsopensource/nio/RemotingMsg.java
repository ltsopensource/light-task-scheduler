package com.github.ltsopensource.nio;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 2/3/16.
 */
public class RemotingMsg implements Serializable{

    private String name;
    private boolean b;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
