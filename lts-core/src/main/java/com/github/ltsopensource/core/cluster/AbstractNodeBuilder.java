package com.github.ltsopensource.core.cluster;

import com.github.ltsopensource.core.listener.MasterChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractNodeBuilder<T extends AbstractJobNode, B extends NodeBuilder> implements NodeBuilder<T> {

    protected List<MasterChangeListener> masterChangeListeners;
    private AtomicBoolean built = new AtomicBoolean(false);
    protected String[] locations;

    public final B setPropertiesConfigure(String... locations) {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("locations can not null");
        }
        this.locations = locations;
        return (B) this;
    }

    public B addMasterChangeListener(MasterChangeListener masterChangeListener) {
        if (masterChangeListener != null) {
            if (masterChangeListeners == null) {
                masterChangeListeners = new ArrayList<MasterChangeListener>();
            }
            masterChangeListeners.add(masterChangeListener);
        }
        return (B) this;
    }

    private void checkLocations() {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("locations can not null");
        }
    }

    public final T build() {
        if (!built.compareAndSet(false, true)) {
            throw new IllegalStateException("Already Built");
        }
        checkLocations();
        T node = build0();
        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                node.addMasterChangeListener(masterChangeListener);
            }
        }
        return node;
    }

    protected abstract T build0();
}
