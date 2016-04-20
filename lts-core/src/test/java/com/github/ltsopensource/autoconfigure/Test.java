package com.github.ltsopensource.autoconfigure;

import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.json.JSONFactory;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 4/18/16.
 */
public class Test {

    public static void main(String[] args) throws IOException {
        TestProperties properties = PropertiesConfigurationFactory.createPropertiesConfiguration(TestProperties.class);
        System.out.println(JSON.toJSONString(properties));

//        JSONFactory.setJSONAdapter("ltsjson");
//        System.out.println(JSON.parse("2321321", Integer.class));
    }

}
