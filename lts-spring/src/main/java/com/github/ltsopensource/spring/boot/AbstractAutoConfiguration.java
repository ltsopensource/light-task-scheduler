package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.AbstractJobNode;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.listener.MasterChangeListener;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.spring.boot.annotation.MasterNodeListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
public abstract class AbstractAutoConfiguration implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAutoConfiguration.class);
    protected ApplicationContext applicationContext;

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        initJobNode();
        injectMasterChangeListeners();
        getJobNode().start();
    }

    @Override
    public final void destroy() throws Exception {
        if (getJobNode() != null) {
            getJobNode().stop();
        }
    }

    private void injectMasterChangeListeners() {
        Map<String, Object> listeners = applicationContext.getBeansWithAnnotation(MasterNodeListener.class);
        if (CollectionUtils.isNotEmpty(listeners)) {
            for (Map.Entry<String, Object> entry : listeners.entrySet()) {
                Object listener = entry.getValue();
                MasterNodeListener annotation = listener.getClass().getAnnotation(MasterNodeListener.class);
                NodeType[] nodeTypes = annotation.nodeTypes();
                boolean ok = false;
                if (nodeTypes != null && nodeTypes.length > 0) {
                    for (NodeType type : nodeTypes) {
                        if (type == nodeType()) {
                            ok = true;
                            break;
                        }
                    }
                } else {
                    ok = true;
                }
                if (!ok) {
                    continue;
                }
                if (listener instanceof MasterChangeListener) {
                    getJobNode().addMasterChangeListener((MasterChangeListener) listener);
                } else {
                    LOGGER.warn(entry.getKey() + "  is not instance of " + MasterChangeListener.class.getName());
                }
            }
        }
    }

    protected abstract void initJobNode();

    protected abstract NodeType nodeType();

    protected abstract AbstractJobNode getJobNode();
}
