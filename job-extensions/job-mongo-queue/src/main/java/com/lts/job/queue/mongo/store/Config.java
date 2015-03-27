package com.lts.job.queue.mongo.store;

import java.util.Arrays;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 * 存储连接配置
 */
public class Config {

    private String[] addresses;
    private String username;
    private String password;
    private String dbName;

    public Config(String[] addresses, String username, String password, String dbName) {
        this.addresses = addresses;
        this.username = username;
        this.password = password;
        this.dbName = dbName;
    }

    public Config() {
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;

        Config config = (Config) o;

        if (!Arrays.equals(addresses, config.addresses)) return false;
        if (dbName != null ? !dbName.equals(config.dbName) : config.dbName != null) return false;
        if (password != null ? !password.equals(config.password) : config.password != null) return false;
        if (username != null ? !username.equals(config.username) : config.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = addresses != null ? Arrays.hashCode(addresses) : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (dbName != null ? dbName.hashCode() : 0);
        return result;
    }
}
