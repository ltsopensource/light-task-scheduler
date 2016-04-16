package com.github.ltsopensource.jvmmonitor.mbean;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public interface JVMInfoMBean {

    Date getStartTime();

    String getJVM();

    String getJavaVersion();

    String getPID();

    String getInputArguments();

    String getJavaHome();

    String getArch();

    String getOSName();

    String getOSVersion();

    String getJavaSpecificationVersion();

    String getJavaLibraryPath();

    String getFileEncode();

    int getAvailableProcessors();

    int getLoadedClassCount();

    long getTotalLoadedClassCount();

    long getUnloadedClassCount();

    long getTotalCompilationTime();

    String  getHostName();

    String getLocalIp();

}
