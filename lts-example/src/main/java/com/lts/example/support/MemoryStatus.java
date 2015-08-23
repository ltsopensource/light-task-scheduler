package com.lts.example.support;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MemoryStatus {

    public static void print() {
            Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        String msg = "Max:" + (maxMemory / 1024 / 1024) + "M, Total:"
                + (totalMemory / 1024 / 1024) + "M, Free:" + (freeMemory / 1024 / 1024)
                + "M, Use:" + ((totalMemory / 1024 / 1024) - (freeMemory / 1024 / 1024)) + "M";
        System.out.println(msg);
    }
}
