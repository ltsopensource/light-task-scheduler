package com.github.ltsopensource.queue.mysql;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.JobQueueUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.AbstractPreLoader;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.queue.mysql.support.RshHolder;
import com.github.ltsopensource.store.jdbc.SqlTemplate;
import com.github.ltsopensource.store.jdbc.SqlTemplateFactory;
import com.github.ltsopensource.store.jdbc.builder.OrderByType;
import com.github.ltsopensource.store.jdbc.builder.SelectSql;
import com.github.ltsopensource.store.jdbc.builder.UpdateSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MysqlPreLoader extends AbstractPreLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlPreLoader.class);
    private SqlTemplate sqlTemplate;

    public MysqlPreLoader(AppContext appContext) {
        super(appContext);
        this.sqlTemplate = SqlTemplateFactory.create(appContext.getConfig());
    }

    @Override
    protected JobPo getJob(String taskTrackerNodeGroup, String jobId) {
        return new SelectSql(sqlTemplate)
                .select()
                .all()
                .from()
                .table(getTableName(taskTrackerNodeGroup))
                .where("job_id = ?", jobId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
    }

    @Override
    protected boolean lockJob(String taskTrackerNodeGroup, String jobId,
                              String taskTrackerIdentity,
                              Long triggerTime,
                              Long gmtModified) {
        try {
            return new UpdateSql(sqlTemplate)
                    .update()
                    .table(getTableName(taskTrackerNodeGroup))
                    .set("is_running", true)
                    .set("task_tracker_identity", taskTrackerIdentity)
                    .set("gmt_modified", SystemClock.now())
                    .where("job_id = ?", jobId)
                    .and("is_running = ?", false)
                    .and("trigger_time = ?", triggerTime)
                    .and("gmt_modified = ?", gmtModified)
                    .doUpdate() == 1;
        } catch (Exception e) {
            LOGGER.error("Error when lock job:" + e.getMessage(), e);
            return false;
        }
    }


    @Override
    protected List<JobPo> load(String loadTaskTrackerNodeGroup, int loadSize) {
        try {
            return new SelectSql(sqlTemplate)
                    .select()
                    .all()
                    .from()
                    .table(getTableName(loadTaskTrackerNodeGroup))
                    .where("is_running = ?", false)
                    .and("trigger_time< ?", SystemClock.now())
                    .orderBy()
                    .column("priority", OrderByType.ASC)
                    .column("trigger_time", OrderByType.ASC)
                    .column("gmt_created", OrderByType.ASC)
                    .limit(0, loadSize)
                    .list(RshHolder.JOB_PO_LIST_RSH);
        } catch (Exception e) {
            LOGGER.error("Error when load job:" + e.getMessage(), e);
            return null;
        }
    }

    private String getTableName(String taskTrackerNodeGroup) {
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
    }
}
