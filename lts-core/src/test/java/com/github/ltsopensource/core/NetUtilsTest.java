package com.github.ltsopensource.core;

import com.github.ltsopensource.core.commons.utils.NetUtils;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class NetUtilsTest {

    @Test
    public void test() {
        System.out.println(NetUtils.getAvailablePort());

        System.out.println(NetUtils.getIpByHost("127.0.0.1"));

        System.out.println(NetUtils.getLocalAddress());

        System.out.println(NetUtils.getLocalHost());

        System.out.println(NetUtils.getRandomPort());
    }

}
