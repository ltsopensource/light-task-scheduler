package com.github.ltsopensource.admin.web.api;

import com.github.ltsopensource.admin.cluster.BackendAppContext;
import com.github.ltsopensource.admin.request.JobQueueReq;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.admin.web.AbstractMVC;
import com.github.ltsopensource.admin.web.support.Builder;
import com.github.ltsopensource.admin.web.vo.RestfulResponse;
import com.github.ltsopensource.biz.logger.JobLogUtils;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.CronExpression;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
@RestController
public class CronJobQueueApi extends AbstractMVC {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronJobQueueApi.class);
    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/cron-job-get")
    public RestfulResponse cronJobGet(JobQueueReq request) {
        PaginationRsp<JobPo> paginationRsp = appContext.getCronJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/cron-job-update")
    public RestfulResponse cronJobUpdate(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        try {
            // 1. 检测 cronExpression是否是正确的
            CronExpression expression = new CronExpression(request.getCronExpression());
            Date nextTriggerTime = expression.getTimeAfter(new Date());
            if (nextTriggerTime == null) {
                return Builder.build(false, StringUtils.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
            }
            JobPo oldJobPo = appContext.getCronJobQueue().getJob(request.getJobId());
            boolean success = appContext.getCronJobQueue().selectiveUpdateByJobId(request);
            if (success) {
                JobPo newJobPo = appContext.getCronJobQueue().getJob(request.getJobId());
                try {
                    // 判断是否有relyOnPrevCycle变更
                    boolean relyOnPrevCycleChanged = !newJobPo.getRelyOnPrevCycle().equals(oldJobPo.getRelyOnPrevCycle());
                    boolean cronExpressionChanged = !newJobPo.getCronExpression().equals(oldJobPo.getCronExpression());

                    // 1. 修改前relyOnPrevCycle=true,并且修改后也是true
                    if (oldJobPo.getRelyOnPrevCycle() && !relyOnPrevCycleChanged) {
                        // 看CronExpression是否有修改,如果有修改,需要更新triggerTime
                        if (cronExpressionChanged) {
                            request.setTriggerTime(nextTriggerTime);
                        }
                        appContext.getExecutableJobQueue().selectiveUpdateByJobId(request);
                    } else {
                        // 2. 需要对批量任务做处理
                        if (relyOnPrevCycleChanged) {
                            // 如果relyOnPrevCycle 修改过
                            if (oldJobPo.getRelyOnPrevCycle()) {
                                // 之前是依赖的,现在不依赖,需要生成批量任务
                                appContext.getExecutableJobQueue().remove(oldJobPo.getTaskTrackerNodeGroup(), oldJobPo.getJobId());
                                appContext.getNoRelyJobGenerator().generateCronJobForInterval(newJobPo, new Date());
                            } else {
                                // 之前不依赖,现在依赖,需要删除批量任务
                                appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                                // 添加新的任务
                                newJobPo.setTriggerTime(nextTriggerTime.getTime());
                                try {
                                    newJobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(newJobPo));
                                    appContext.getExecutableJobQueue().add(newJobPo);
                                } catch (DupEntryException ignored) {
                                }
                            }
                        } else {
                            // 如果relyOnPrevCycle 没有修改过, 表示relyOnPrevCycle=false, 那么要看cronExpression是否修改过,如果修改过,需要删除重新生成
                            if (cronExpressionChanged) {
                                appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                                appContext.getNoRelyJobGenerator().generateCronJobForInterval(newJobPo, new Date());
                            } else {
                                appContext.getExecutableJobQueue().selectiveUpdateByTaskId(request);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
                }
                response.setSuccess(true);
            } else {
                return Builder.build(false, "该任务已经被删除或者执行完成");
            }
            JobLogUtils.log(LogType.UPDATE, oldJobPo, appContext.getJobLogger());
            return response;
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "请输入正确的 CronExpression!" + e.getMessage());
        }
    }

    @RequestMapping("/job-queue/cron-job-delete")
    public RestfulResponse cronJobDelete(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getCronJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(true, "已经删除");
        }
        boolean success = appContext.getCronJobQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        JobLogUtils.log(LogType.DEL, jobPo, appContext.getJobLogger());

        return Builder.build(true, "ok");
    }

    @RequestMapping("/job-queue/cron-job-suspend")
    public RestfulResponse cronJobSuspend(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getCronJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }
        try {
            jobPo.setGmtModified(SystemClock.now());
            appContext.getSuspendJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "该任务已经被暂停, 请检查暂停队列");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "移动任务到暂停队列失败, error:" + e.getMessage());
        }
        try {
            appContext.getCronJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除Cron任务失败，请手动删除! error:" + e.getMessage());
        }
        try {
            if (!jobPo.getRelyOnPrevCycle()) {
                appContext.getCronJobQueue().updateLastGenerateTriggerTime(jobPo.getJobId(), new Date().getTime());
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            } else {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
        }

        // 记录日志
        JobLogUtils.log(LogType.SUSPEND, jobPo, appContext.getJobLogger());

        return Builder.build(true, "ok");
    }
}
