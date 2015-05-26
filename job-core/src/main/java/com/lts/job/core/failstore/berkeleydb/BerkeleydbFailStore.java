package com.lts.job.core.failstore.berkeleydb;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.KVPair;
import com.lts.job.core.failstore.FailStore;
import com.lts.job.core.failstore.FailStoreException;
import com.lts.job.core.file.FileUtils;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.JSONUtils;
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

    public BerkeleydbFailStore(Config config) {
        try {
            envHome = FileUtils.createDirIfNotExist(config.getFailStorePath());
            envConfig = new EnvironmentConfig();
            // 如果不存在则创建一个
            envConfig.setAllowCreate(true);
            // 以只读方式打开，默认为false
            envConfig.setReadOnly(false);
            // 事务支持,如果为true，则表示当前环境支持事务处理，默认为false，不支持事务处理
            envConfig.setTransactional(true);
            // Configures the durability associated with transactions.
            envConfig.setDurability(Durability.COMMIT_SYNC);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open() throws FailStoreException {
        environment = new Environment(envHome, envConfig);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setSortedDuplicates(false);
        dbConfig.setTransactional(true);
        try {
            db = environment.openDatabase(null, "lts", dbConfig);
        } catch (DatabaseException e) {
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
                    // do nothing
                    LOGGER.warn("close cursor failed! ", e);
                }
            }
        }
    }

    @Override
    public void close() throws FailStoreException {
        // do nothing
        try {
            if (db != null) {
                db.close();
            }
            if (environment != null) {
                environment.cleanLog();
                environment.close();
            }
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void destroy() throws FailStoreException {
        environment.removeDatabase(null, db.getDatabaseName());
        environment.close();
    }
}
