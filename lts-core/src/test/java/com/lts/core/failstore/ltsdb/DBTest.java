package com.lts.core.failstore.ltsdb;

import com.lts.core.failstore.ltsdb.iterator.DBIterator;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class DBTest {


    private DB<String, String> getDB() {
        String path = System.getProperty("user.home") + "/tmp/ltsdb";

        final DB<String, String> db =
                new DBBuilder<String, String>()
                        .setPath(path)
                        .create();
        return db;
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
