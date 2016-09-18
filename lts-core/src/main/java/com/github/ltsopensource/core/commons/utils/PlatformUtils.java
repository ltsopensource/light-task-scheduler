package com.github.ltsopensource.core.commons.utils;

/**
 * @author Robert HG (254963746@qq.com) on 4/6/16.
 */
public class PlatformUtils {

    private static final String OPERATING_SYSTEM;
    private static final boolean isWindows;
    private static final boolean isOSX;
    private static final boolean isLinux;

    static {
        OPERATING_SYSTEM = _getOperatingSystem();
        isWindows = "windows".equals(OPERATING_SYSTEM);
        isOSX = "osx".equals(OPERATING_SYSTEM);
        isLinux = "linux".equals(OPERATING_SYSTEM);
    }

    public static String getOperatingSystem() {
        return OPERATING_SYSTEM;
    }

    private static String _getOperatingSystem() {
        String name = System.getProperty("os.name").toLowerCase().trim();
        if (name.startsWith("linux")) {
            return "linux";
        }
        if (name.startsWith("mac os x")) {
            return "osx";
        }
        if (name.startsWith("win")) {
            return "windows";
        }
        return name.replaceAll("\\W+", "_");
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isOSX() {
        return isOSX;
    }

    public static boolean isLinux() {
        return isLinux;
    }

}
