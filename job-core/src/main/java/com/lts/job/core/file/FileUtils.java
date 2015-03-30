package com.lts.job.core.file;

import java.io.File;
import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class FileUtils {

    public static File createFileIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            // 创建父目录
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("create file[" + file.getAbsolutePath() + "] failed!", e);
            }
        }
        return file;
    }

    public static File createDirIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            // 创建父目录
            file.getParentFile().mkdirs();
            file.mkdir();
        }
        return file;
    }
}
