package com.lts.queue;

import com.lts.core.AppContext;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiKey;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
@SPI(key = SpiKey.JOB_QUEUE, dftValue = "mysql")
public interface PreLoaderFactory {

    public PreLoader getPreLoader(AppContext appContext);

}
