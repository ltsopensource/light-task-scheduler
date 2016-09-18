package com.github.ltsopensource.remoting.serialize;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.spi.ServiceLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class AdaptiveSerializable implements RemotingSerializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemotingSerializable.class);

    private static volatile String defaultSerializable;

    private static final Map<Integer, RemotingSerializable>
            ID_SERIALIZABLE_MAP = new HashMap<Integer, RemotingSerializable>();

    static {
        Set<String> names = ServiceLoader.getServiceProviders(RemotingSerializable.class);
        for (String name : names) {
            if (!Constants.ADAPTIVE.equalsIgnoreCase(name)) {
                RemotingSerializable serializable = ServiceLoader.load(RemotingSerializable.class, name);
                ID_SERIALIZABLE_MAP.put(serializable.getId(), serializable);
            }
        }
    }

    public static RemotingSerializable getSerializableById(int id) {
        return ID_SERIALIZABLE_MAP.get(id);
    }

    public static void setDefaultSerializable(String defaultSerializable) {
        AdaptiveSerializable.defaultSerializable = defaultSerializable;
        LOGGER.info("Using defaultSerializable [{}]", defaultSerializable);
    }

    private RemotingSerializable getRemotingSerializable() {
        RemotingSerializable remotingSerializable;

        String serializable = defaultSerializable; // copy reference
        if (serializable != null) {
            remotingSerializable = ServiceLoader.load(RemotingSerializable.class, serializable);
        } else {
            remotingSerializable = ServiceLoader.loadDefault(RemotingSerializable.class);
        }
        return remotingSerializable;
    }

    @Override
    public int getId() {
        return getRemotingSerializable().getId();
    }

    @Override
    public byte[] serialize(Object obj) throws Exception {
        return getRemotingSerializable().serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        return getRemotingSerializable().deserialize(data, clazz);
    }
}
