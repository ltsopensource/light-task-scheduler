package com.github.ltsopensource.core.failstore.ltsdb;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.kv.DB;
import com.github.ltsopensource.kv.DBBuilder;
import com.github.ltsopensource.kv.Entry;
import com.github.ltsopensource.kv.iterator.DBIterator;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.Options;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class DBTest {

    String path = System.getProperty("user.home") + "/tmp/dbtest";

    private DB<String, String> getDB() {

        try {
            FileUtils.createDirIfNotExist(path + "/ltsdb");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final DB<String, String> db =
                new DBBuilder<String, String>()
                        .setPath(path + "/ltsdb")
                        .create();
        return db;
    }

    @Test
    public void testLtsDbPut() {

        DB<String, String> db = getDB();
        db.init();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            db.put("testKey" + i, "testvalue" + i);
        }
        // 4597 待优化
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testLeveldbPut() throws IOException {

        org.iq80.leveldb.DB db;

        Options options;
        options = new Options();
        options.createIfMissing(true);
        options.cacheSize(100 * 1024 * 1024);   // 100M
        options.maxOpenFiles(400);
        FileUtils.createDirIfNotExist(new File(path + "/leveldb"));
        JniDBFactory.factory.repair(new File(path + "/leveldb"), options);
        db = JniDBFactory.factory.open(new File(path + "/leveldb"), options);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            db.put(("testKey" + i).getBytes("UTF-8"), ("testvalue" + i).getBytes("UTF-8"));
        }
        //972
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testPutGet() {

        DB<String, String> db = getDB();

        db.init();

        db.put("111", "aaaa");

        db.put("222", "bbbb");

        System.out.println(db.get("111"));

        System.out.println(db.get("222"));

        DBIterator<Entry<String, String>> iterator = db.iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        db.close();
    }

    @Test
    public void testReload() {

        DB<String, String> db = getDB();

        db.init();

        System.out.println(db.get("111"));

        System.out.println(db.get("222"));

        DBIterator<Entry<String, String>> iterator = db.iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        db.close();

    }

    @Test
    public void testRemove() {

        DB<String, String> db = getDB();

        db.init();

        System.out.println(db.get("111"));

        System.out.println(db.get("222"));

        DBIterator<Entry<String, String>> iterator = db.iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        db.remove("222");

        System.out.println("111=" + db.get("111"));

        System.out.println("222=" + db.get("222"));

        iterator = db.iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        db.close();

    }


    @Test
    public void testSequence() {

        DB<String, String> db = getDB();

        db.init();

        for (int i = 0; i < 100; i++) {
            db.put("idx_" + i, String.valueOf(i));
        }

        DBIterator<Entry<String, String>> iterator = db.iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        db.close();
    }
}
