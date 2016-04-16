package com.github.ltsopensource.nio.loop;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.nio.NioException;
import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.processor.NioProcessor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 1/17/16.
 */
public class NioSelectorLoop {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioSelectorLoop.class);
    private static final int SELECTOR_AUTO_REBUILD_THRESHOLD = 512;
    private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
    private Selector selector;
    private SelectorWorker selectorWorker;
    private volatile boolean running = false;

    private static boolean isLinuxPlatform = false;

    static {
        if (Constants.OS_NAME != null && Constants.OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }

        /**
         * Selector.open() can throw a NPE in java6 because of missing synchronization.
         * http://bugs.java.com/view_bug.do?bug_id=6427854
         */
        String key = "sun.nio.ch.bugLevel";
        try {
            String bugLevel = System.getProperty(key);
            if (bugLevel == null) {
                System.setProperty(key, "");
            }
        } catch (SecurityException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to get/set System Property: {}", key, e);
            }
        }
    }

    public NioSelectorLoop(String name, NioProcessor processor) {
        this.selector = openSelector();
        this.selectorWorker = new SelectorWorker(name, processor);
    }

    private static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    private Selector openSelector() {
        Selector result = null;
        // 在linux平台，尽量启用epoll实现
        if (isLinuxPlatform()) {
            try {
                final Class<?> providerClazz = Class.forName("sun.nio.ch.EPollSelectorProvider");
                if (providerClazz != null) {
                    final Method method = providerClazz.getMethod("provider");
                    if (method != null) {
                        final SelectorProvider selectorProvider = (SelectorProvider) method.invoke(null);
                        if (selectorProvider != null) {
                            result = selectorProvider.openSelector();
                        }
                    }
                }
            } catch (final Exception ignored) {
            }
        }

        if (result == null) {
            try {
                result = SelectorProvider.provider().openSelector();
            } catch (IOException e) {
                throw new NioException("open selector error:" + e.getMessage(), e);
            }
        }
        return result;
    }

    public Selector selector() {
        return selector;
    }

    public void start() {
        running = true;
        selectorWorker.start();
    }

    public void shutdown() {
        running = false;
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        try {
            int selectCnt = 0;
            long currentNanoTime = System.nanoTime();
            long selectDeadLineNanos = currentNanoTime + TimeUnit.SECONDS.toNanos(1);
            for (; ; ) {
                long timeoutMillis = (selectDeadLineNanos - currentNanoTime + 500000L) / 1000000L;
                if (timeoutMillis <= 0) {
                    if (selectCnt == 0) {
                        selector.selectNow();
                        selectCnt = 1;
                    }
                    break;
                }

                int selectedKeys = selector.select(timeoutMillis);
                selectCnt++;

                if (selectedKeys != 0) {
                    break;
                }

                if (selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                    LOGGER.warn("Selector.select() returned prematurely {} times in a row; rebuilding selector.", selectCnt);

                    rebuildSelector();
                    selector = this.selector;

                    // 重新select,填充 selectedKeys
                    selector.selectNow();
                    selectCnt = 1;
                    break;
                }

                currentNanoTime = System.nanoTime();
            }

            if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Selector.select() returned prematurely {} times in a row.", selectCnt - 1);
                }
            }
        } catch (CancelledKeyException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector - JDK bug?", e);
            }
        }
    }

    /**
     * 重新创建一个新的Selector, 来解决 java nio 在linux下 epoll CPU 100% 的bug
     * 官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK1.7版本该问题仍旧存在，只不过该bug发生概率降低了一些而已，它并没有被根本解决
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6403933
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=2147719
     */
    private void rebuildSelector() {

        final Selector oldSelector = this.selector;
        final Selector newSelector;

        if (oldSelector == null) {
            return;
        }

        try {
            newSelector = openSelector();
        } catch (Exception e) {
            LOGGER.warn("Failed to create a new Selector.", e);
            return;
        }

        // 注册所有的channels到新的Selector.
        int nChannels = 0;
        for (; ; ) {
            try {
                for (SelectionKey key : oldSelector.keys()) {
                    Object a = key.attachment();
                    try {
                        if (key.channel().keyFor(newSelector) != null) {
                            continue;
                        }

                        int interestOps = key.interestOps();
                        key.cancel();
                        key.channel().register(newSelector, interestOps, a);
                        nChannels++;
                    } catch (Exception e) {
                        LOGGER.warn("Failed to re-register a Channel to the new Selector.", e);
                    }
                }
            } catch (ConcurrentModificationException e) {
                // Probably due to concurrent modification of the key set.
                continue;
            }

            break;
        }

        selector = newSelector;

        try {
            // time to close the old selector as everything else is registered to the new one
            oldSelector.close();
        } catch (Throwable t) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to close the old Selector.", t);
            }
        }

        LOGGER.info("Migrated " + nChannels + " channel(s) to the new Selector.");
    }

    private class SelectorWorker extends Thread {

        private NioProcessor processor;

        public SelectorWorker(String name, NioProcessor processor) {
            super(name);
            setDaemon(true);
            this.processor = processor;
        }

        @Override
        public void run() {

            while (running) {

                try {
                    select();

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();

                    if (selectionKeys.isEmpty()) {
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {

                        final SelectionKey key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            doAccept(key);
                        }

                        if (key.isConnectable()) {
                            doConnect(key);
                        }

                        if (key.isValid() && key.isReadable()) {
                            doRead(key);
                        }

                        if (key.isValid() && key.isWritable()) {
                            doWrite(key);
                        }
                    }

                } catch (Throwable t) {
                    LOGGER.warn("Unexpected exception in the selector loop.", t);

                    // 睡眠1S, 防止连续的异常导致cpu消耗
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }

        private void doAccept(SelectionKey key) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("doAccept:" + key.toString());
            }
            processor.accept(key);
        }

        private void doConnect(SelectionKey key) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("doConnect:" + key.toString());
            }
            processor.connect(key);
        }

        private void doRead(SelectionKey key) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("doRead:" + key.toString());
            }
            NioChannel channel = (NioChannel) key.attachment();
            processor.read(channel);
        }

        private void doWrite(SelectionKey key) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("doWrite:" + key.toString());
            }
            NioChannel channel = (NioChannel) key.attachment();
            processor.flush(channel);
        }
    }

}
