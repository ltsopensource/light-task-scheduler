package com.lts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Robert HG (254963746@qq.com) on 9/25/15.
 */
public class H2DatabaseTest {

    //数据库连接URL
    private static final String JDBC_URL = "jdbc:h2:mem:test";
    //连接数据库时使用的用户名
    private static final String USER = "ttt";
    //连接数据库时使用的密码
    private static final String PASSWORD = "123";
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS = "org.h2.Driver";

    public static void main(String[] args) throws Exception {
        // 加载H2数据库驱动
        Class.forName(DRIVER_CLASS);
        // 根据连接URL，用户名，密码获取数据库连接
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement();
        //如果存在USER_INFO表就先删除USER_INFO表
        stmt.execute("DROP TABLE IF EXISTS lts_executable_job_queue_test_trade_TaskTracker");
        //创建USER_INFO表
        stmt.execute("CREATE TABLE `lts_executable_job_queue_test_trade_TaskTracker` (" +
                "  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID与业务无关的'," +
                "  `job_id` varchar(64) DEFAULT NULL COMMENT '记录ID,程序生成的'," +
                "  `priority` int(11) DEFAULT NULL COMMENT '优先级(数值越大，优先级越低)'," +
                "  `retry_times` int(11) DEFAULT '0' COMMENT '重试次数'," +
                "  `task_id` varchar(64) DEFAULT NULL COMMENT '客户端传过来的任务ID'," +
                "  `gmt_created` bigint(20) DEFAULT NULL COMMENT '创建时间'," +
                "  `gmt_modified` bigint(11) DEFAULT NULL COMMENT '修改时间'," +
                "  `submit_node_group` varchar(64) DEFAULT NULL COMMENT '提交客户端的节点组'," +
                "  `task_tracker_node_group` varchar(64) DEFAULT NULL COMMENT '执行job 的任务节点'," +
                "  `ext_params` text COMMENT '额外参数 JSON'," +
                "  `is_running` tinyint(11) DEFAULT NULL COMMENT '是否正在执行'," +
                "  `task_tracker_identity` varchar(64) DEFAULT NULL COMMENT '执行的taskTracker的唯一标识'," +
                "  `need_feedback` tinyint(4) DEFAULT NULL COMMENT '是否需要反馈给客户端'," +
                "  `cron_expression` varchar(32) DEFAULT NULL COMMENT '执行时间表达式 (和 quartz 表达式一样)'," +
                "  `trigger_time` bigint(20) DEFAULT NULL COMMENT '下一次执行时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `idx_job_id` (`job_id`)," +
                "  UNIQUE KEY `idx_taskId_taskTrackerNodeGroup` (`task_id`,`task_tracker_node_group`)" +
                ") ");
        //新增
        stmt.executeUpdate("INSERT INTO `lts_executable_job_queue_test_trade_TaskTracker` (`id`, `job_id`, `priority`, `retry_times`, `task_id`, `gmt_created`, `gmt_modified`, `submit_node_group`, `task_tracker_node_group`, `ext_params`, `is_running`, `task_tracker_identity`, `need_feedback`, `cron_expression`, `trigger_time`)" +
                "VALUES" +
                "\t(3229, '21F8C97BB4447533D017FC767F5F2741', 100, 0, 'FC9D1FC6BC3F40AEA07E3065980E83ED', 1443099768226, 1443099768226, 'test_jobClient', 'test_trade_TaskTracker', '{\\\"shopId\\\":\\\"111\\\"}', 0, NULL, 1, '0 0/20 * * * ?', 1443100800000);");
        //查询
        ResultSet rs = stmt.executeQuery("SELECT * FROM lts_executable_job_queue_test_trade_TaskTracker");
        //遍历结果集
        while (rs.next()) {
            System.out.println(rs.getString("id") + "," + rs.getString("job_Id") + "," + rs.getString("task_id"));
        }
        //释放资源
        stmt.close();
        //关闭连接
        conn.close();
    }


}
