package com.lts.job.store;


import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 */
public class ConfigLoader {

    private static final String ADDRESSES_KEY = "mongo.addresses";
    private static final String DB_NAME_KEY = "mongo.dbname";
    private static final String USERNAME_KEY = "mongo.username";
    private static final String PASSWORD_KEY = "mongo.password";

    public static Config getConfig(){

        ResourceBundle resource = ResourceBundle.getBundle("mongo", Locale.getDefault());

        Config config = new Config();
        config.setDbName(resource.getString(DB_NAME_KEY));
        config.setUsername(resource.getString(USERNAME_KEY));
        config.setPassword(resource.getString(PASSWORD_KEY));
        String addresses = resource.getString(ADDRESSES_KEY);
        config.setAddresses(addresses.split(","));
        return config;
    }

}
