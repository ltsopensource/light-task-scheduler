package com.lts.biz.logger.mysql;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.cluster.Config;
import com.lts.core.constant.Level;
import com.lts.core.support.SystemClock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/12/15.
 */
public class MysqlJobLoggerTest {

    @Test
    public void testLog() throws Exception {

        Config config = new Config();
        // 任务队列用mysql
        config.setParameter("job.queue", "mysql");
        // mysql 配置
        config.setParameter("jdbc.url", "jdbc:mysql://127.0.0.1:3306/lts");
        config.setParameter("jdbc.username", "root");
        config.setParameter("jdbc.password", "root");
        MysqlJobLogger mysqlJobLogger = new MysqlJobLogger(config);

        List<JobLogPo> jobLogPoList = new ArrayList<JobLogPo>();

        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setGmtCreated(SystemClock.now());
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogPo.setLogType(LogType.BIZ);
        jobLogPoList.add(jobLogPo);

        mysqlJobLogger.log(jobLogPoList);

    }
}