package com.lts.core.cmd;

import com.lts.core.commons.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public class HttpCmdContext {

    private final Map<String, HttpCmdProcessor> processorMap = new HashMap<String, HttpCmdProcessor>();

    public void addCmdProcessor(HttpCmdProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("processor can not be null");
        }

        String command = processor.getCommand();

        if (StringUtils.isEmpty(command)) {
            throw new IllegalArgumentException("processor.command can not be null");
        }
        processorMap.put(command, processor);
    }

    public HttpCmdProcessor getCmdProcessor(String command) {
        return processorMap.get(command);
    }

}
