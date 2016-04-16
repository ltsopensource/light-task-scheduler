package com.github.ltsopensource.core.failstore;

import com.github.ltsopensource.core.commons.file.FileLock;
import com.github.ltsopensource.core.commons.file.FileUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 7/5/15.
 */
public abstract class AbstractFailStore implements FailStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailStore.class);

    protected FileLock fileLock;

    private String home;
    protected File dbPath;
    private static final String dbLockName = "___db.lock";

    public AbstractFailStore(File dbPath, boolean needLock) {
        try {
            this.dbPath = dbPath;
            String path = dbPath.getPath();
            this.home = path.substring(0, path.indexOf(getName())).concat(getName());
            if (needLock) {
                getLock(dbPath.getPath());
            }
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getLock(String failStorePath) throws IOException {
        //  get sequence
        FileUtils.createDirIfNotExist(failStorePath);
        // 有可能两个进程同时创建这个目录，所以用文件锁来得到控制权
        fileLock = new FileLock(failStorePath.concat("/").concat(dbLockName));
        boolean locked = fileLock.tryLock();    // 1s
        if (!locked) {
            throw new IllegalStateException("can not get current file lock.");
        }
        LOGGER.info("Current failStore path is {}", failStorePath);
        return failStorePath;
    }

    public List<FailStore> getDeadFailStores() {
        File homeDir = new File(home);
        File[] subFiles = homeDir.listFiles();
        List<FailStore> deadFailStores = new ArrayList<FailStore>();
        if (subFiles != null && subFiles.length != 0) {
            for (File subFile : subFiles) {
                try {
                    if (subFile.isDirectory()) {
                        FileLock tmpLock = new FileLock(subFile.getPath().concat("/").concat(dbLockName));
                        boolean locked = tmpLock.tryLock();
                        if (locked) {
                            // 能获得锁，说明这个目录锁对应的节点已经down了
                            FailStore failStore = getFailStore(subFile);
                            if (failStore != null) {
                                deadFailStores.add(failStore);
                            }
                            tmpLock.release();
                        }
                    }
                } catch (Exception e) {
                    // ignore
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return deadFailStores;
    }

    private FailStore getFailStore(File dbPath) {
        try {
            Constructor<? extends FailStore> constructor = this.getClass().getConstructor(File.class, boolean.class);
            return constructor.newInstance(dbPath, false);
        } catch (Exception e) {
            LOGGER.error("new instance failStore failed,", e);
        }
        return null;
    }

    protected abstract void init() throws FailStoreException;

    protected abstract String getName();

    @Override
    public String getPath() {
        return dbPath.getPath();
    }
}
