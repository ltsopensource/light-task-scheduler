package com.lts.startup;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JettyContainer {

    public static void main(String[] args) {
        try {
            String confPath = args[0];
            Integer port = Integer.parseInt(args[1]);

            System.setProperty("lts.admin.config.path", confPath + "/conf");

            Server server = new Server(port);
            WebAppContext webapp = new WebAppContext();
            webapp.setWar(confPath + "/lts-admin.war");
            server.setHandler(webapp);
            server.setStopAtShutdown(true);
            server.start();

            System.out.println("LTS-Admin started. http://" + NetUtils.getLocalHost() + ":" + port + "/main.html");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
