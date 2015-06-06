package com.lts.job.store.jdbc;

import com.lts.job.core.cluster.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class DataSourceProviderFactoryTest {

    public static void main(String[] args) throws SQLException {
        Config config = new Config();
        config.setParameter("jdbc.datasource.provider", "hsqldb");
        config.setParameter("jdbc.url", "jdbc:hsqldb:mem:lts");
        config.setParameter("jdbc.username", "sa");
        config.setParameter("jdbc.password", "");

        DataSourceProvider dataSourceProvider = DataSourceProviderFactory.create(config);
        DataSource dataSource = dataSourceProvider.getDataSource(config);

        Connection connection = dataSource.getConnection();

//        connection.prepareStatement("drop table barcodes if exists;").execute();
        connection.prepareStatement("CREATE TABLE lts_node (" +
                "  identity varchar(32) NOT NULL," +
                "  available tinyint ," +
                "  clusterName varchar(64) ," +
                "  nodeType varchar(16) ," +
                "  ip varchar(16) ," +
                "  port int ," +
                "  nodeGroup varchar(64) ," +
                "  createTime bigint ," +
                "  threads int ," +
                "  PRIMARY KEY (identity)," +
                "  KEY idx_clusterName (clusterName)" +
                ");").execute();

//        connection.prepareStatement("create table barcodes (id integer, barcode varchar(20) not null);").execute();
        connection.prepareStatement("insert into barcodes (id, barcode)"
                + "values (1, '12345566');").execute();

        // query from the db
        ResultSet rs = connection.prepareStatement("select id, barcode  from barcodes;").executeQuery();
        rs.next();
        System.out.println(String.format("ID: %1d, Name: %1s", rs.getInt(1), rs.getString(2)));
    }

}