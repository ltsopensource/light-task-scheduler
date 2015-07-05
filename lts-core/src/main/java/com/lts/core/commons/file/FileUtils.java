package com.lts.core.commons.file;

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

    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            //若目录下没有文件则直接删除
            File[] delFiles = file.listFiles();
            if (delFiles == null || delFiles.length == 0) {
                file.delete();
            } else {
                for (File delFile : delFiles) {
                    if (delFile.isDirectory()) {
                        delete(delFile);
                    }
                    delFile.delete();
                }
            }
        }
        // 自己也删除
        file.delete();
    }

}
