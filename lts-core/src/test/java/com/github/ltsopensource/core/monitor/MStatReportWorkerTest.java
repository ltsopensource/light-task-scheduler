package com.github.ltsopensource.core.monitor;

import com.github.ltsopensource.core.domain.monitor.MData;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Robert HG (254963746@qq.com) on 7/13/16.
 */
public class MStatReportWorkerTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(MStatReportWorker.class);

    private PriorityBlockingQueue<MData> queue = new PriorityBlockingQueue<MData>(16, new Comparator<MData>() {
        @Override
        public int compare(MData o1, MData o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    });

    private final static int MAX_RETRY_RETAIN = 30;
    private final static int BATCH_REPORT_SIZE = 10;
    private volatile boolean running = false;

    @Test
    public void testReport() {

        for (int i = 0; i < 100; i++) {
            MData mData = new MData();
            mData.setTimestamp(Long.valueOf(i));
            report(mData);
        }

    }

    private void report(MData mData) {

        int size = queue.size();

        if (size >= MAX_RETRY_RETAIN) {
            int needRemoveSize = size - (MAX_RETRY_RETAIN - 1);
            for (int i = 0; i < needRemoveSize; i++) {
                queue.poll();
            }
        }
        queue.add(mData);

        while (queue.size() > 0) {
            List<MData> list = new ArrayList<MData>();
            queue.drainTo(list, BATCH_REPORT_SIZE);

            boolean success = false;
            try {

                if (sendReq()) {
                    success = true;
                }
            } catch (Throwable t) {
                LOGGER.warn("Report monitor data Error : " + t.getMessage(), t);
            } finally {
                if (!success) {
                    // 放回去
                    queue.addAll(list);
                }
            }
            if (!success) {
                // 停止while
                break;
            }
        }
    }

    private boolean sendReq() {
        return false;
    }
}