package com.lts.job.core.file;

import java.io.*;

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

    public static String read(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder createTableSql = new StringBuilder();
        String data = null;
        while ((data = br.readLine()) != null) {
            createTableSql.append(data);
        }
        return createTableSql.toString();
    }

}
