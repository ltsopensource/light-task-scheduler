package com.lts.job.core.failstore;

import com.lts.job.core.domain.KVPair;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by hugui on 5/21/15.
 */
public interface FailStore {

    public void open() throws FailStoreException;

    public void put(String key, Object value) throws FailStoreException;

    public void delete(String key) throws FailStoreException;

    public void delete(List<String> keys) throws FailStoreException;

    public <T> List<KVPair<String, T>> fetchTop(int size, Type type) throws FailStoreException;

    public void close() throws FailStoreException;

    public void destroy() throws FailStoreException;
}
