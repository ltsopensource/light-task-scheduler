package com.github.ltsopensource.store.mongo;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryImpl;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTemplate.class);

    private AdvancedDatastore ds;
    // default DBCollection name
    private String defaultCollName;

    public MongoTemplate(AdvancedDatastore ds) {
        this.ds = ds;
    }

    private String getCollName(String collName) {
        if (StringUtils.isNotEmpty(collName)) {
            return collName;
        }
        if (StringUtils.isNotEmpty(getDefaultCollName())) {
            return getDefaultCollName();
        }
        throw new IllegalArgumentException("collName can not be null!");
    }

    public <T> Query<T> createQuery(final String collName, final Class<T> clazz) {
        DBCollection dbCollection = ds.getDB().getCollection(getCollName(collName));
        return new QueryImpl<T>(clazz, dbCollection, ds);
    }

    public <T> Query<T> createQuery(final Class<T> clazz) {
        return createQuery(null, clazz);
    }

    public <T> Key<T> save(final String collName, final T entity) {
        return ds.save(getCollName(collName), entity);
    }

    public <T> Key<T> save(final T entity) {
        return save(null, entity);
    }


    public DBCollection getCollection(String collName) {
        return ds.getDB().getCollection(getCollName(collName));
    }

    public DBCollection getCollection() {
        return getCollection(null);
    }

    public void ensureIndex(String collName, String name, String fields) {
        ensureIndex(collName, name, fields, false, false);

    }

    public void ensureIndex(String name, String fields) {
        ensureIndex(null, name, fields);
    }

    public void ensureIndex(String name, String fields, boolean unique, boolean dropDupsOnCreate) {
        ensureIndex(null, name, fields, unique, dropDupsOnCreate);
    }

    /**
     * Ensures (creating if necessary) the index including the field(s) + directions; eg fields = "field1, -field2" ({field1:1, field2:-1})
     */
    public void ensureIndex(String collName, String name, String fields, boolean unique, boolean dropDupsOnCreate) {

        BasicDBObject dbFields = parseFieldsString(fields);

        final BasicDBObjectBuilder keyOpts = new BasicDBObjectBuilder();
        if (name != null && name.length() != 0) {
            keyOpts.add("name", name);
        }
        if (unique) {
            keyOpts.add("unique", true);
            if (dropDupsOnCreate) {
                keyOpts.add("dropDups", true);
            }
        }

        final DBCollection dbColl = getCollection(getCollName(collName));

        final BasicDBObject opts = (BasicDBObject) keyOpts.get();
        if (opts.isEmpty()) {
            LOGGER.debug("Ensuring index for " + dbColl.getName() + " with keys:" + dbFields);
            dbColl.createIndex(dbFields);
        } else {
            LOGGER.debug("Ensuring index for " + dbColl.getName() + " with keys:" + fields + " and opts:" + opts);
            dbColl.createIndex(dbFields, opts);
        }
    }

    private BasicDBObject parseFieldsString(final String fields) {
        BasicDBObjectBuilder ret = BasicDBObjectBuilder.start();
        final String[] parts = fields.split(",");
        for (String s : parts) {
            s = s.trim();
            int dir = 1;
            if (s.startsWith("-")) {
                dir = -1;
                s = s.substring(1).trim();
            }
            ret = ret.add(s, dir);
        }
        return (BasicDBObject) ret.get();
    }

    public String getDefaultCollName() {
        return defaultCollName;
    }

    public void setDefaultCollName(String defaultCollName) {
        this.defaultCollName = defaultCollName;
    }

    public <T> UpdateOperations<T> createUpdateOperations(final Class<T> clazz) {
        return ds.createUpdateOperations(clazz);
    }

    public <T> T findAndDelete(Query<T> query) {
        return ds.findAndDelete(query);
    }

    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> ops) {
        return findAndModify(query, ops, false);
    }

    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> ops, final boolean oldVersion) {
        return findAndModify(query, ops, oldVersion, false);
    }

    public <T> T findAndModify(final Query<T> query, final UpdateOperations<T> ops, final boolean oldVersion, final boolean createIfMissing) {
        return ds.findAndModify(query, ops, oldVersion, createIfMissing);
    }

    public <T> WriteResult delete(final Query<T> query) {
        return ds.delete(query);
    }

    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> ops, final boolean createIfMissing) {
        return ds.update(query, ops, createIfMissing);
    }

    public <T> UpdateResults update(final Query<T> query, final UpdateOperations<T> ops) {
        return ds.update(query, ops);
    }

    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> ops, final boolean createIfMissing) {
        return ds.updateFirst(query, ops, createIfMissing);
    }

    public <T> UpdateResults updateFirst(final Query<T> query, final UpdateOperations<T> ops) {
        return ds.updateFirst(query, ops);
    }

    public <T> long getCount(Query<T> query){
        return ds.getCount(query);
    }
}
