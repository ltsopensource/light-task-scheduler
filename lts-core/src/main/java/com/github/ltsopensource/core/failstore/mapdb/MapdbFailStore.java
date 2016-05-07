package com.github.ltsopensource.core.failstore.mapdb;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.failstore.AbstractFailStore;
import com.github.ltsopensource.core.failstore.FailStoreException;
import com.github.ltsopensource.core.json.JSON;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * see http://www.mapdb.org/
 *
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public class MapdbFailStore extends AbstractFailStore {

    public static final String name = "mapdb";
    private DB db;
    private ConcurrentNavigableMap<String, String> map;

    public MapdbFailStore(File dbPath, boolean needLock) {
        super(dbPath, needLock);
    }

    @Override
    protected void init() throws FailStoreException {
        try {
            String dbName = dbPath.getPath() + "/lts.db";
            db = DBMaker.fileDB(new File(dbName))
                    .closeOnJvmShutdown()
                    .encryptionEnable("lts")
                    .make();
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void open() throws FailStoreException {
        try {
            map = db.treeMap("lts");
            db.commit();
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        try {
            String valueString = JSON.toJSONString(value);
            map.put(key, valueString);
            // persist changes into disk
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(String key) throws FailStoreException {
        try {
            map.remove(key);
            // persist changes into disk
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(List<String> keys) throws FailStoreException {
        if (keys == null || keys.size() == 0) {
            return;
        }
        try {
            for (String key : keys) {
                map.remove(key);
            }
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw new FailStoreException(e);
        }
    }

    @Override
    public <T> List<Pair<String, T>> fetchTop(int size, Type type) throws FailStoreException {

        List<Pair<String, T>> list = new ArrayList<Pair<String, T>>(size);
        if (map.size() == 0) {
            return list;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            T value = JSON.parse(entry.getValue(), type);
            Pair<String, T> pair = new Pair<String, T>(key, value);
            list.add(pair);
            if (list.size() >= size) {
                break;
            }
        }
        return list;
    }

    @Override
    public void close() throws FailStoreException {
        try {
            db.close();
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void destroy() throws FailStoreException {
        try {
            close();
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            FileUtils.delete(dbPath);
        }
    }
}
