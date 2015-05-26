package com.lts.job.core.failstore.leveldb;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.KVPair;
import com.lts.job.core.failstore.FailStore;
import com.lts.job.core.failstore.FailStoreException;
import com.lts.job.core.file.FileAccessor;
import com.lts.job.core.file.FileException;
import com.lts.job.core.file.FileUtils;
import com.lts.job.core.util.JSONUtils;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Only a single process (possibly multi-threaded) can access a particular database at a time
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
public class LeveldbFailStore implements FailStore {

    // 文件锁 (同一时间只能有一个线程访问leveldb文件)
    private FileAccessor dbLock;
    /**
     * 数据库目录
     */
    private File dbPath;

    private DB db;

    private Options options;

    public LeveldbFailStore(Config config) {
        dbPath = FileUtils.createDirIfNotExist(config.getFailStorePath());
        options = new Options();
        try {
            dbLock = new FileAccessor(config.getFailStorePath() + "___db.lock");
            dbLock.createIfNotExist();
        } catch (FileException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open() throws FailStoreException {
        dbLock.tryLock();
        try {
            db = JniDBFactory.factory.open(dbPath, options);
        } catch (IOException e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        try {
            String valueString = JSONUtils.toJSONString(value);
            db.put(key.getBytes("UTF-8"), valueString.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(String key) throws FailStoreException {
        try {
            if (key == null) {
                return;
            }
            db.delete(key.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(List<String> keys) throws FailStoreException {
        if (keys == null || keys.size() == 0) {
            return;
        }
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public <T> List<KVPair<String, T>> fetchTop(int size, Type type) throws FailStoreException {
        try {
            List<KVPair<String, T>> list = new ArrayList<KVPair<String, T>>(size);
            DBIterator iterator = db.iterator();
            for (iterator.seekToLast(); iterator.hasPrev(); iterator.prev()) {
                String key = new String(iterator.peekPrev().getKey(), "UTF-8");
                T value = JSONUtils.parse(new String(iterator.peekPrev().getValue(), "UTF-8"), type);
                KVPair<String, T> pair = new KVPair<String, T>(key, value);
                list.add(pair);
                if (list.size() >= size) {
                    break;
                }
            }
            return list;
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void close() throws FailStoreException {
        try {
            db.close();
            dbLock.unlock();
        } catch (IOException e) {
            throw new FailStoreException(e);
        }
    }

    public void destroy() throws FailStoreException {
        try {
            JniDBFactory.factory.destroy(dbPath, options);
            dbLock.delete();
        } catch (IOException e) {
            throw new FailStoreException(e);
        }
    }
}
