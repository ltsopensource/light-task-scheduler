package com.github.ltsopensource.kv;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.kv.serializer.JsonStoreSerializer;
import com.github.ltsopensource.kv.serializer.StoreSerializer;

import java.io.File;

/**
 * @author Robert HG (254963746@qq.com) on 12/15/15.
 */
public class DBBuilder<K, V> {

    private StoreSerializer serializer;
    private StoreConfig storeConfig;

    public DBBuilder() {
        storeConfig = new StoreConfig();
    }

    public DB<K, V> create() {
        if (serializer == null) {
            this.serializer = new JsonStoreSerializer();
        }
        if (storeConfig.getDbPath() == null) {
            storeConfig.setDbPath(new File(Constants.USER_HOME));
        }

        storeConfig.setDataPath(new File(storeConfig.getDbPath(), "data"));
        storeConfig.setLogPath(new File(storeConfig.getDbPath(), "logs"));
        storeConfig.setIndexPath(new File(storeConfig.getDbPath(), "index"));

        return new DBImpl<K, V>(serializer, storeConfig);
    }


    public DBBuilder<K, V> setPath(File path) {
        this.storeConfig.setDbPath(path);
        return this;
    }

    public DBBuilder<K, V> setPath(String path) {
        this.storeConfig.setDbPath(new File(path));
        return this;
    }

    public DBBuilder<K, V> setSerializer(StoreSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

}
