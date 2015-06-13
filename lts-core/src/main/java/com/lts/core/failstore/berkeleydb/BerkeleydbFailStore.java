package com.lts.core.failstore.berkeleydb;

import com.lts.core.commons.file.FileLock;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.domain.KVPair;
import com.lts.core.failstore.FailStore;
import com.lts.core.failstore.FailStoreException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.sleepycat.je.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 5/26/15.
 */
public class BerkeleydbFailStore implements FailStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(BerkeleydbFailStore.class);
    private Environment environment;
    private Database db;
    private EnvironmentConfig envConfig;
    private File envHome;
    private FileLock lock;
    private DatabaseConfig dbConfig;

    public BerkeleydbFailStore(String failStorePath) {
        try {
            failStorePath = failStorePath + "berkeleydb/";
            envHome = FileUtils.createDirIfNotExist(failStorePath);
            envConfig = new EnvironmentConfig();
            // 如果不存在则创建一个
            envConfig.setAllowCreate(true);
            // 以只读方式打开，默认为false
            envConfig.setReadOnly(false);
            // 事务支持,如果为true，则表示当前环境支持事务处理，默认为false，不支持事务处理
            envConfig.setTransactional(true);
            // Configures the durability associated with transactions.
            envConfig.setDurability(Durability.COMMIT_SYNC);

            dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setSortedDuplicates(false);
            dbConfig.setTransactional(true);

            lock = new FileLock(failStorePath + "___db.lock");

        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open() throws FailStoreException {
        try {
            lock.tryLock();
            environment = new Environment(envHome, envConfig);
            db = environment.openDatabase(null, "lts", dbConfig);
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        try {
            String valueString = JSONUtils.toJSONString(value);
            OperationStatus status = db.put(null, new DatabaseEntry(key.getBytes("UTF-8")),
                    new DatabaseEntry(valueString.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(String key) throws FailStoreException {
        try {
            DatabaseEntry delKey = new DatabaseEntry();
            delKey.setData(key.getBytes("UTF-8"));
            OperationStatus status = db.delete(null, delKey);
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(List<String> keys) throws FailStoreException {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public <T> List<KVPair<String, T>> fetchTop(int size, Type type) throws FailStoreException {
        Cursor cursor = null;
        try {
            List<KVPair<String, T>> list = new ArrayList<KVPair<String, T>>();

            cursor = db.openCursor(null, CursorConfig.DEFAULT);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundValue = new DatabaseEntry();
            while (cursor.getNext(foundKey, foundValue, LockMode.DEFAULT) ==
                    OperationStatus.SUCCESS) {
                String key = new String(foundKey.getData(), "UTF-8");
                String valueString = new String(foundValue.getData(), "UTF-8");

                T value = JSONUtils.parse(valueString, type);
                KVPair<String, T> pair = new KVPair<String, T>(key, value);
                list.add(pair);
                if (list.size() >= size) {
                    break;
                }
            }
            return list;
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (DatabaseException e) {
                    LOGGER.warn("close cursor failed! ", e);
                }
            }
        }
    }

    @Override
    public void close() throws FailStoreException {
        try {
            if (db != null) {
                db.close();
            }
            if (environment != null && environment.isValid()) {
                environment.cleanLog();
                environment.close();
            }
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            lock.release();
        }
    }

    @Override
    public void destroy() throws FailStoreException {
        try {
            if (environment != null) {
                environment.removeDatabase(null, db.getDatabaseName());
                environment.close();
            }
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            lock.delete();
        }
    }
}
