package com.lts.queue.mysql;

import com.lts.core.AppContext;
import com.lts.queue.PreLoader;
import com.lts.queue.PreLoaderFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MysqlPreLoaderFactory implements PreLoaderFactory {
    @Override
    public PreLoader getPreLoader(AppContext appContext) {
        return new MysqlPreLoader(appContext);
    }
}
