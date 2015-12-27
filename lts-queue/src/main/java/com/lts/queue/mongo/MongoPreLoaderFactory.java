package com.lts.queue.mongo;

import com.lts.core.Application;
import com.lts.queue.PreLoader;
import com.lts.queue.PreLoaderFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MongoPreLoaderFactory implements PreLoaderFactory {
    @Override
    public PreLoader getPreLoader(Application application) {
        return new MongoPreLoader(application);
    }
}
