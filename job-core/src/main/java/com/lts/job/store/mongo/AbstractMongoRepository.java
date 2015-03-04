package com.lts.job.store.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.mongodb.WriteResult;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         通用的mongo存储类
 */
public abstract class AbstractMongoRepository<T> {

    protected static Datastore ds;

    protected AbstractMongoRepository() {
        ds = DatastoreHolder.getDatastore();
    }

    private Class<T> clazz = getSuperClassGenericType(this.getClass());

    public Key<T> save(T t) {
        return ds.save(t);
    }

    public Iterable<Key<T>> save(List<T> list) {
        return ds.save(list);
    }

    public WriteResult delete(Map<String, Object> map) {
        return ds.delete(createQuery(map));
    }

    public WriteResult delete(Query<T> query) {
        return ds.delete(query);
    }

    public Datastore getDs() {
        return ds;
    }

    public List<T> findList(Map<String, Object> map) {
        return createQuery(map).asList();
    }


    public List<T> findList(Map<String, Object> map, int offset, int limit) {
        return createQuery(map, offset, limit).asList();
    }

    public T get(Map<String, Object> map) {
        List<T> list = this.findList(map, 0, 1);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<T> findList(Query<T> query) {
        return query.asList();
    }

    public long getCount(Map<String, Object> map) {
        return ds.getCount(createQuery(map));
    }

    public Query<T> createQuery(Map<String, Object> map) {
        Query<T> query = ds.createQuery(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            query.field(entry.getKey()).equal(entry.getValue());
        }
        return query;
    }

    public Query<T> createQuery(Map<String, Object> map, String order) {
        Query<T> query = ds.createQuery(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            query.field(entry.getKey()).equal(entry.getValue());
        }
        query.order(order);
        return query;
    }

    public Query<T> createQuery() {
        return ds.createQuery(clazz);
    }

    public Query<T> createQuery(Map<String, Object> map, int offset, int limit) {
        Query<T> query = ds.createQuery(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            query.field(entry.getKey()).equal(entry.getValue());
        }
        query.offset(offset);
        query.limit(limit);
        return query;
    }

    private Class getSuperClassGenericType(Class clazz) {
        int index = 0;
        Type genType = clazz.getGenericSuperclass();//得到泛型父类
        //如果没有实现ParameterizedType接口，即不支持泛型，直接返回Object.class
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

}