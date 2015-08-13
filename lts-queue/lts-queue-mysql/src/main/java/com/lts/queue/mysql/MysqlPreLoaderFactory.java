package com.lts.queue.mysql;

import com.lts.core.Application;
import com.lts.core.cluster.Config;
import com.lts.queue.PreLoader;
import com.lts.queue.PreLoaderFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MysqlPreLoaderFactory implements PreLoaderFactory {
    @Override
    public PreLoader getPreLoader(Config config, Application application) {
        return new MysqlPreLoader(application);
    }
}
