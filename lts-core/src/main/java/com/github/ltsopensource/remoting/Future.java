package com.github.ltsopensource.remoting;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public interface Future {

    boolean isSuccess();

    Throwable cause();

}
