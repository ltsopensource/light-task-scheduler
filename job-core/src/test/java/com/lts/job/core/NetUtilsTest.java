package com.lts.job.core;

import com.lts.job.core.util.NetUtils;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class NetUtilsTest {

    @Test
    public void test() {
        System.out.println(NetUtils.getAvailablePort());

        System.out.println(NetUtils.getIpByHost("localhost"));

        System.out.println(NetUtils.getLocalAddress());

        System.out.println(NetUtils.getLocalHost());

        System.out.println(NetUtils.getRandomPort());
    }

}
