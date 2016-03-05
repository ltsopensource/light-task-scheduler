package com.lts.core.cmd;

import com.lts.core.commons.utils.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class HttpCmdContext {

    private ReentrantLock lock = new ReentrantLock();
    private final Map<String/*节点标识*/, Map<String/*cmd*/, HttpCmdProcessor>>
            NODE_PROCESSOR_MAP = new HashMap<String, Map<String, HttpCmdProcessor>>();

    public void addCmdProcessor(HttpCmdProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("processor can not be null");
        }

        String identity = processor.nodeIdentity();
        Assert.hasText(identity, "nodeIdentity can't be empty");

        String command = processor.getCommand();
        Assert.hasText(command, "command can't be empty");

        Map<String, HttpCmdProcessor> cmdProcessorMap = NODE_PROCESSOR_MAP.get(identity);
        if (cmdProcessorMap == null) {
            lock.lock();
            if (cmdProcessorMap == null) {
                cmdProcessorMap = new ConcurrentHashMap<String, HttpCmdProcessor>();
                NODE_PROCESSOR_MAP.put(identity, cmdProcessorMap);
            }
            lock.unlock();
        }
        cmdProcessorMap.put(command, processor);
    }

    public HttpCmdProcessor getCmdProcessor(String nodeIdentity, String command) {
        Assert.hasText(nodeIdentity, "nodeIdentity can't be empty");

        Map<String, HttpCmdProcessor> cmdProcessorMap = NODE_PROCESSOR_MAP.get(nodeIdentity);
        if (cmdProcessorMap == null) {
            return null;
        }
        return cmdProcessorMap.get(command);
    }

}
