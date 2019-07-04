package com.github.ltsopensource.startup.tasktracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
@Log4j2
public class LTSXmlApplicationContext extends AbstractXmlApplicationContext {

    private Resource[] configResources;

    public LTSXmlApplicationContext(String[] paths) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<Resource>();
        if (paths != null && paths.length > 0) {
            for (String path : paths) {
                try {
                    Resource[] resources = resolver.getResources(path);
                    if (resources != null && resources.length > 0) {
                        Collections.addAll(resourceList, resources);
                    }
                } catch (IOException e) {
                   log.error("resolve resource error: [path={}]", path, e);
                }
            }
        }

        configResources = new Resource[resourceList.size()];
        resourceList.toArray(configResources);

        refresh();
    }

    @Override
    protected Resource[] getConfigResources() {
        return configResources;
    }
}
