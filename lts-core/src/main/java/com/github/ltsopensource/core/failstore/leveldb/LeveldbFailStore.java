package com.github.ltsopensource.core.failstore.leveldb;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.failstore.AbstractFailStore;
import com.github.ltsopensource.core.failstore.FailStoreException;
import com.github.ltsopensource.core.json.JSON;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Only a single process (possibly multi-threaded) can access a particular database at a time
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
public class LeveldbFailStore extends AbstractFailStore {

    private DB db;

    private Options options;

    public static final String name = "leveldb";

    public LeveldbFailStore(File dbPath, boolean needLock) {
        super(dbPath, needLock);
    }

    @Override
    protected void init() throws FailStoreException {
        try {
            options = new Options();
            options.createIfMissing(true);
            options.cacheSize(100 * 1024 * 1024);   // 100M
            options.maxOpenFiles(400);
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    protected String getName() {
        return name;
    }

    @Override
    public void open() throws FailStoreException {
        try {
            JniDBFactory.factory.repair(dbPath, options);
            db = JniDBFactory.factory.open(dbPath, options);
        } catch (IOException e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        try {
            String valueString = JSON.toJSONString(value);
            assert valueString != null;
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
        WriteBatch batch = db.createWriteBatch();
        try {

            for (String key : keys) {
                batch.delete(key.getBytes("UTF-8"));
            }
            db.write(batch);
        } catch (UnsupportedEncodingException e) {
            throw new FailStoreException(e);
        } finally {
            try {
                batch.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public <T> List<Pair<String, T>> fetchTop(int size, Type type) throws FailStoreException {
        Snapshot snapshot = db.getSnapshot();
        DBIterator iterator = null;
        try {
            List<Pair<String, T>> list = new ArrayList<Pair<String, T>>(size);
            ReadOptions options = new ReadOptions();
            options.snapshot(snapshot);
            iterator = db.iterator(options);
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                Map.Entry<byte[], byte[]> entry = iterator.peekNext();
                String key = new String(entry.getKey(), "UTF-8");
                T value = JSON.parse(new String(entry.getValue(), "UTF-8"), type);
                Pair<String, T> pair = new Pair<String, T>(key, value);
                list.add(pair);
                if (list.size() >= size) {
                    break;
                }
            }
            return list;
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException ignored) {
                }
            }
            try {
                snapshot.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() throws FailStoreException {
        try {
            if (db != null) {
                db.close();
            }
        } catch (IOException e) {
            throw new FailStoreException(e);
        }
    }

    public void destroy() throws FailStoreException {
        try {
            close();
            JniDBFactory.factory.destroy(dbPath, options);
        } catch (IOException e) {
            throw new FailStoreException(e);
        } finally {
            if (fileLock != null) {
                fileLock.release();
            }
            FileUtils.delete(dbPath);
        }
    }

}
