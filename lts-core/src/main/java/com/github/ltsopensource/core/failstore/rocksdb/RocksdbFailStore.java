package com.github.ltsopensource.core.failstore.rocksdb;

import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.failstore.AbstractFailStore;
import com.github.ltsopensource.core.failstore.FailStoreException;
import com.github.ltsopensource.core.json.JSON;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class RocksdbFailStore extends AbstractFailStore {

    private RocksDB db = null;
    private Options options;

    public RocksdbFailStore(File dbPath, boolean needLock) {
        super(dbPath, needLock);
    }

    public static final String name = "rocksdb";

    @Override
    protected void init() throws FailStoreException {
        try {
            options = new Options();
            options.setCreateIfMissing(true)
                    .setWriteBufferSize(8 * SizeUnit.KB)
                    .setMaxWriteBufferNumber(3)
                    .setMaxBackgroundCompactions(10)
                    .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
                    .setCompactionStyle(CompactionStyle.UNIVERSAL);

            Filter bloomFilter = new BloomFilter(10);
            BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
            tableConfig.setBlockCacheSize(64 * SizeUnit.KB)
                    .setFilter(bloomFilter)
                    .setCacheNumShardBits(6)
                    .setBlockSizeDeviation(5)
                    .setBlockRestartInterval(10)
                    .setCacheIndexAndFilterBlocks(true)
                    .setHashIndexAllowCollision(false)
                    .setBlockCacheCompressedSize(64 * SizeUnit.KB)
                    .setBlockCacheCompressedNumShardBits(10);

            options.setTableFormatConfig(tableConfig);
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
            db = RocksDB.open(options, dbPath.getPath());
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        String valueString = JSON.toJSONString(value);
        WriteOptions writeOpts = new WriteOptions();
        try {
            writeOpts.setSync(true);
            writeOpts.setDisableWAL(true);
            db.put(writeOpts, key.getBytes("UTF-8"), valueString.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            writeOpts.dispose();
        }
    }

    @Override
    public void delete(String key) throws FailStoreException {
        try {
            db.remove(key.getBytes("UTF-8"));
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
    public <T> List<Pair<String, T>> fetchTop(int size, Type type) throws FailStoreException {
        RocksIterator iterator = null;
        try {
            List<Pair<String, T>> list = new ArrayList<Pair<String, T>>(size);
            iterator = db.newIterator();
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                iterator.status();
                String key = new String(iterator.key(), "UTF-8");
                T value = JSON.parse(new String(iterator.value(), "UTF-8"), type);
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
                iterator.dispose();
            }
        }
    }

    @Override
    public void close() throws FailStoreException {
        try {
            if (db != null) {
                db.close();
            }
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void destroy() throws FailStoreException {
        try {
            db.close();
            options.dispose();
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            if (fileLock != null) {
                fileLock.release();
            }
            FileUtils.delete(dbPath);
        }
    }
}
