package com.github.ltsopensource.startup.admin;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JettyContainer {

    public static void main(String[] args) {
        try {
            String confPath = args[0];

            confPath = confPath.trim();

            Properties conf = new Properties();
            InputStream is = new FileInputStream(new File(confPath + "/conf/lts-admin.cfg"));
            conf.load(is);
            String port = conf.getProperty("port");
            if (port == null || port.trim().equals("")) {
                port = "8081";
            }

            String contextPath = conf.getProperty("contextPath");
            if (contextPath == null || contextPath.trim().equals("")) {
                contextPath = "/";
            }

            Server server = new Server(Integer.parseInt(port));
            WebAppContext webapp = new WebAppContext();
            webapp.setWar(confPath + "/war/lts-admin.war");
            webapp.setContextPath(contextPath);
            Map<String, String> initParams = new HashMap<String, String>();
            initParams.put("lts.admin.config.path", confPath + "/conf");
            webapp.setInitParams(initParams);
            server.setHandler(webapp);
            server.setStopAtShutdown(true);
            server.start();

            System.out.println("LTS-Admin started. http://" + NetUtils.getLocalHost() + ":" + port + (contextPath == "/" ? "" : contextPath) + "/index.htm");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
