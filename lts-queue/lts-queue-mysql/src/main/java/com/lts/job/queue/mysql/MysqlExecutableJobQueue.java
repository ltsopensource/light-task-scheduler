package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.file.FileUtils;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.DateUtils;
import com.lts.job.core.util.JobQueueUtils;
import com.lts.job.core.util.StringUtils;
import com.lts.job.queue.ExecutableJobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.queue.mysql.support.ResultSetHandlerHolder;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutableJobQueue extends AbstractMysqlJobQueue implements ExecutableJobQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlExecutableJobQueue.class);

    // 这里做一下流控, 尽量减小 mysql dead lock 的概率
    private Semaphore semaphore;
    private long acquireTimeout = 2000; // 2s
    // 用来缓存SQL，不用每次去生成，可以重用
    private final ConcurrentHashMap<String, String> SQL_CACHE_MAP = new ConcurrentHashMap<String, String>();

    public MysqlExecutableJobQueue(Config config) {
        super(config);
        int permits = config.getParameter(Constants.JOB_TAKE_PARALLEL_SIZE, Constants.DEFAULT_JOB_TAKE_PARALLEL_SIZE);
        if (permits <= 10) {
            permits = Constants.DEFAULT_JOB_TAKE_PARALLEL_SIZE;
        }
        this.acquireTimeout = config.getParameter(Constants.JOB_TAKE_ACQUIRE_TIMEOUT, acquireTimeout);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    protected String getTableName(JobQueueRequest request) {
        if (StringUtils.isEmpty(request.getTaskTrackerNodeGroup())) {
            throw new IllegalArgumentException(" takeTrackerNodeGroup cat not be null");
        }
        return getTableName(request.getTaskTrackerNodeGroup());
    }

    @Override
    public boolean createQueue(String taskTrackerNodeGroup) {
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_executable_job_queue.sql");
            String sql = FileUtils.read(is);
            getSqlTemplate().update(getRealSql(sql, taskTrackerNodeGroup));
            return true;
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
    }

    private String getTableName(String taskTrackerNodeGroup) {
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
    }

    private String getRealSql(String sql, String taskTrackerNodeGroup) {
        String key = sql.concat(taskTrackerNodeGroup);
        String fineSQL = SQL_CACHE_MAP.get(key);
        // 这里可以不用锁，多生成一次也不会产生什么问题
        if (fineSQL == null) {
            fineSQL = sql.replace("{tableName}", getTableName(taskTrackerNodeGroup));
            SQL_CACHE_MAP.put(key, fineSQL);
        }
        return fineSQL;
    }

    @Override
    public boolean add(JobPo jobPo) {
        jobPo.setGmtCreated(DateUtils.currentTimeMillis());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        try {
            return super.add(getTableName(jobPo.getTaskTrackerNodeGroup()), jobPo);
        } catch (JobQueueException e) {
            if (e.getMessage().contains("doesn't exist Query:")) {
                createQueue(jobPo.getTaskTrackerNodeGroup());
                add(jobPo);
            }
        }
        return true;
    }

    private String takeSelectSQL = "SELECT *" +
            " FROM `{tableName}` " +
            " WHERE is_running = ? " +
            " AND `trigger_time` < ? " +
            " ORDER BY `trigger_time` ASC, `priority` ASC, `gmt_created` ASC " +
            " LIMIT 0, 1";

    private String taskUpdateSQL = "UPDATE `{tableName}` SET " +
            "`is_running` = ?, " +
            "`task_tracker_identity` = ?, " +
            "`gmt_modified` = ?" +
            " WHERE job_id = ? AND is_running = ?";

    @Override
    public JobPo take(final String taskTrackerNodeGroup, final String taskTrackerIdentity) {
        try {
            boolean acquire = semaphore.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS);
            if (!acquire) {
                // 直接返回null
                return null;
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Try to take job failed.", e);
        }
        try {
            /**
             * 这里从SELECT FOR UPDATE 优化为 CAS 乐观锁
             */
            Long now = DateUtils.currentTimeMillis();
            Object[] selectParams = new Object[]{false, now};

            JobPo jobPo = getSqlTemplate().query(getRealSql(takeSelectSQL, taskTrackerNodeGroup),
                    ResultSetHandlerHolder.JOB_PO_RESULT_SET_HANDLER, selectParams);
            if (jobPo == null) {
                return null;
            }

            Object[] params = new Object[]{
                    true, taskTrackerIdentity, now, jobPo.getJobId(), false
            };
            // 返回影响的行数
            int affectedRow = 0;
            try {
                affectedRow = getSqlTemplate().update(getRealSql(taskUpdateSQL, taskTrackerNodeGroup), params);
            } catch (SQLException e) {
                //  dead lock ignore
                if (e.getMessage().contains("Deadlock found when trying to get lock")) {
                    return null;
                }
                throw e;
            }
            if (affectedRow == 0) {
                return take(taskTrackerNodeGroup, taskTrackerIdentity);
            } else {
                jobPo.setIsRunning(true);
                jobPo.setTaskTrackerIdentity(taskTrackerIdentity);
                jobPo.setGmtModified(now);
                return jobPo;
            }
        } catch (SQLException e) {
            throw new JobQueueException(e);
        } finally {
            semaphore.release();
        }
    }

    private String removeSQL = "DELETE FROM `{tableName}` WHERE job_id = ?";

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        try {
            return getSqlTemplate().update(getRealSql(removeSQL, taskTrackerNodeGroup), jobId) == 1;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String resumeSQL = "UPDATE `{tableName}` SET " +
            "`is_running` = ?," +
            "`task_tracker_identity` = ?," +
            "`gmt_modified` = ?" +
            " WHERE job_id = ? ";

    @Override
    public void resume(JobPo jobPo) {
        try {
            Object[] params = new Object[]{false, null, System.currentTimeMillis(), jobPo.getJobId()};
            getSqlTemplate().update(getRealSql(resumeSQL, jobPo.getTaskTrackerNodeGroup()), params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String getDeadJobSQL = "SELECT * FROM `{tableName}` WHERE is_running = ? AND gmt_modified < ?";

    @Override
    public List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
        try {
            return getSqlTemplate().query(getRealSql(getDeadJobSQL, taskTrackerNodeGroup), ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER, true, deadline);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }
}
