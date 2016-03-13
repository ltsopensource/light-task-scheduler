package com.lts.admin.web.api;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.request.JobQueueReq;
import com.lts.admin.response.PaginationRsp;
import com.lts.admin.support.I18nManager;
import com.lts.admin.web.AbstractMVC;
import com.lts.admin.web.vo.RestfulResponse;
import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.biz.logger.domain.LogType;
import com.lts.cmd.DefaultHttpCmd;
import com.lts.cmd.HttpCmd;
import com.lts.cmd.HttpCmdClient;
import com.lts.cmd.HttpCmdResponse;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.cmd.HttpCmdNames;
import com.lts.core.commons.utils.Assert;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Level;
import com.lts.core.domain.Job;
import com.lts.core.domain.KVPair;
import com.lts.core.json.JSON;
import com.lts.core.support.CronExpression;
import com.lts.core.support.CronExpressionUtils;
import com.lts.core.support.JobDomainConverter;
import com.lts.core.support.SystemClock;
import com.lts.queue.domain.JobPo;
import com.lts.store.jdbc.exception.DupEntryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@RestController
public class JobQueueApi extends AbstractMVC {

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

    @RequestMapping("/job-queue/executable-job-get")
    public RestfulResponse executableJobGet(JobQueueReq request) {
        PaginationRsp<JobPo> paginationRsp = appContext.getExecutableJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executing-job-get")
    public RestfulResponse executingJobGet(JobQueueReq request) {
        PaginationRsp<JobPo> paginationRsp = appContext.getExecutingJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/suspend-job-get")
    public RestfulResponse suspendJobGet(JobQueueReq request) {
        PaginationRsp<JobPo> paginationRsp = appContext.getSuspendJobQueue().pageSelect(request);
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
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }
        // 1. 检测 cronExpression是否是正确的
        try {
            CronExpression expression = new CronExpression(request.getCronExpression());
            if (expression.getTimeAfter(new Date()) == null) {
                response.setSuccess(false);
                response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
                return response;
            }

            boolean success = appContext.getCronJobQueue().selectiveUpdate(request);
            if (success) {
                try {
                    // 把等待执行的队列也更新一下
                    request.setTriggerTime(expression.getTimeAfter(new Date()));
                    appContext.getExecutableJobQueue().selectiveUpdate(request);
                } catch (Exception e) {
                    response.setSuccess(false);
                    response.setMsg("更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
                    return response;
                }
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                response.setMsg("该任务已经被删除或者执行完成.");
            }
            return response;
        } catch (ParseException e) {
            response.setSuccess(false);
            response.setMsg("请输入正确的 CronExpression!");
            return response;
        }
    }

    @RequestMapping("/job-queue/executable-job-update")
    public RestfulResponse executableJobUpdate(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        // 1. 检测 cronExpression是否是正确的
        if (StringUtils.isNotEmpty(request.getCronExpression())) {
            try {
                CronExpression expression = new CronExpression(request.getCronExpression());
                if (expression.getTimeAfter(new Date()) == null) {
                    response.setSuccess(false);
                    response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                    return response;
                }
            } catch (ParseException e) {
                response.setSuccess(false);
                response.setMsg("请输入正确的 CronExpression!");
                return response;
            }
        }
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }
        boolean success = appContext.getExecutableJobQueue().selectiveUpdate(request);
        if (success) {
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setCode("DELETE_OR_RUNNING");
        }
        return response;
    }

    @RequestMapping("/job-queue/suspend-job-update")
    public RestfulResponse suspendJobUpdate(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }
        // 1. 检测 cronExpression是否是正确的
        try {
            CronExpression expression = new CronExpression(request.getCronExpression());
            if (expression.getTimeAfter(new Date()) == null) {
                response.setSuccess(false);
                response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
                return response;
            }

            boolean success = appContext.getSuspendJobQueue().selectiveUpdate(request);
            if (success) {
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                response.setMsg("该任务已经被删除或者执行完成.");
            }
            return response;
        } catch (ParseException e) {
            response.setSuccess(false);
            response.setMsg("请输入正确的 CronExpression!");
            return response;
        }
    }

    @RequestMapping("/job-queue/cron-job-delete")
    public RestfulResponse cronJobDelete(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        if (StringUtils.isEmpty(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("JobId 必须传!");
            return response;
        }
        boolean success = appContext.getCronJobQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMsg("删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
                return response;
            }
        }
        response.setSuccess(true);
        return response;
    }

    @RequestMapping("/job-queue/executable-job-delete")
    public RestfulResponse executableJobDelete(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }

        boolean success = appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
        if (success) {
            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                // 是Cron任务, Cron任务队列的也要被删除
                try {
                    appContext.getCronJobQueue().remove(request.getJobId());
                } catch (Exception e) {
                    response.setSuccess(false);
                    response.setMsg("在Cron任务队列中删除该任务失败，请手动更新! error:" + e.getMessage());
                    return response;
                }
            }
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setMsg("更新失败，该条任务可能已经删除.");
        }

        return response;
    }

    @RequestMapping("/job-queue/suspend-job-delete")
    public RestfulResponse suspendJobDelete(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        if (StringUtils.isEmpty(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("JobId 必须传!");
            return response;
        }
        boolean success = appContext.getSuspendJobQueue().remove(request.getJobId());
        response.setSuccess(success);
        return response;
    }

    @RequestMapping("/job-logger/job-logger-get")
    public RestfulResponse jobLoggerGet(JobLoggerRequest request) {
        RestfulResponse response = new RestfulResponse();

//        try {
//            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
//            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
//        } catch (IllegalArgumentException e) {
//            response.setSuccess(false);
//            response.setMsg(e.getMessage());
//            return response;
//        }

        PaginationRsp<JobLogPo> paginationRsp = appContext.getJobLogger().search(request);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());

        response.setSuccess(true);
        return response;
    }

    /**
     * 给JobTracker发消息 加载任务到内存
     */
    @RequestMapping("/job-queue/load-add")
    public RestfulResponse loadJob(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();

        String nodeGroup = request.getTaskTrackerNodeGroup();

        HttpCmd httpCmd = new HttpCmd();
        httpCmd.setCommand(HttpCmdNames.HTTP_CMD_LOAD_JOB);
        httpCmd.addParam("nodeGroup", nodeGroup);

        List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.JOB_TRACKER);
        if (CollectionUtils.isEmpty(jobTrackerNodeList)) {
            response.setMsg(I18nManager.getMessage("job.tracker.not.found"));
            response.setSuccess(false);
            return response;
        }

        boolean success = false;
        HttpCmdResponse cmdResponse = null;
        for (Node node : jobTrackerNodeList) {
            // 所有的JobTracker都load一遍
            httpCmd.setNodeIdentity(node.getIdentity());
            cmdResponse = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
            if (cmdResponse.isSuccess()) {
                success = true;
            }
        }
        if (success) {
            response.setMsg("Load success");
        } else {
            response.setMsg("Load failed");
        }
        response.setSuccess(success);
        return response;
    }

    @RequestMapping("/job-queue/job-add")
    public RestfulResponse jobAdd(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 表单check

        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
            if (request.getNeedFeedback()) {
                Assert.hasLength(request.getSubmitNodeGroup(), "submitNodeGroup不能为空!");
            }

            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                try {
                    CronExpression expression = new CronExpression(request.getCronExpression());
                    Date nextTime = expression.getTimeAfter(new Date());
                    if (nextTime == null) {
                        response.setSuccess(false);
                        response.setMsg(StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                        return response;
                    } else {
                        request.setTriggerTime(nextTime);
                    }
                } catch (ParseException e) {
                    response.setSuccess(false);
                    response.setMsg("请输入正确的 CronExpression!");
                    return response;
                }
            }

        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            return response;
        }

        KVPair<Boolean, String> pair = addJob(request);
        response.setSuccess(pair.getKey());
        response.setMsg(pair.getValue());
        return response;
    }

    @RequestMapping("/job-queue/cron-job-suspend")
    public RestfulResponse cronJobSuspend(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        if (StringUtils.isEmpty(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("JobId 必须传!");
            return response;
        }
        JobPo jobPo = appContext.getCronJobQueue().finish(request.getJobId());
        if (jobPo == null) {
            response.setSuccess(false);
            response.setMsg("任务不存在，或者已经删除");
            return response;
        }
        try {
            jobPo.setGmtModified(SystemClock.now());
            appContext.getSuspendJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            response.setSuccess(false);
            response.setMsg("改任务已经被暂停, 请检查暂停队列");
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg("移动任务到暂停队列失败, error:" + e.getMessage());
            return response;
        }
        try {
            appContext.getCronJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg("删除Cron任务失败，请手动删除! error:" + e.getMessage());
            return response;
        }
        try {
            appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg("删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
            return response;
        }

        // 记录日志
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.SUSPEND);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        appContext.getJobLogger().log(jobLogPo);

        response.setSuccess(true);
        return response;
    }

    @RequestMapping("/job-queue/suspend-job-recovery")
    public RestfulResponse suspendJobRecovery(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        if (StringUtils.isEmpty(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("JobId 必须传!");
            return response;
        }

        JobPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            response.setSuccess(false);
            response.setMsg("任务不存在，或者已经删除");
            return response;
        }

        // 先恢复,才能删除
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime != null) {
            jobPo.setGmtModified(SystemClock.now());
            try {
                // 1.add to cron job queue
                appContext.getCronJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                response.setSuccess(false);
                response.setMsg("Cron队列中任务已经存在，请检查");
                return response;
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMsg("插入Cron队列中任务错误, error:" + e.getMessage());
                return response;
            }

            try {
                // 2. add to executable queue
                jobPo.setTriggerTime(nextTriggerTime.getTime());
                appContext.getExecutableJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                response.setSuccess(false);
                response.setMsg("等待执行队列中任务已经存在，请检查");
                return response;
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMsg("插入等待执行队列中任务错误, error:" + e.getMessage());
                return response;
            }
        } else {
            response.setSuccess(false);
            response.setMsg("该任务已经无效, 或者已经没有下一轮执行时间点");
            return response;
        }

        // 从暂停表中移除
        if (!appContext.getSuspendJobQueue().remove(request.getJobId())) {
            response.setSuccess(false);
            response.setMsg("恢复暂停任务失败，请重试");
            return response;
        }

        // 记录日志
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.RESUME);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        appContext.getJobLogger().log(jobLogPo);

        response.setSuccess(true);
        return response;
    }

    private KVPair<Boolean, String> addJob(JobQueueReq request) {

        Job job = new Job();
        job.setTaskId(request.getTaskId());
        if (CollectionUtils.isNotEmpty(request.getExtParams())) {
            for (Map.Entry<String, String> entry : request.getExtParams().entrySet()) {
                job.setParam(entry.getKey(), entry.getValue());
            }
        }
        // 执行节点的group名称
        job.setTaskTrackerNodeGroup(request.getTaskTrackerNodeGroup());
        job.setSubmitNodeGroup(request.getSubmitNodeGroup());

        job.setNeedFeedback(request.getNeedFeedback());
        job.setReplaceOnExist(true);
        // 这个是 cron expression 和 quartz 一样，可选
        job.setCronExpression(request.getCronExpression());
        if (request.getTriggerTime() != null) {
            job.setTriggerTime(request.getTriggerTime().getTime());
        }
        job.setPriority(request.getPriority());

        return addJob(job);
    }

    private KVPair<Boolean, String> addJob(Job job) {
        HttpCmd httpCmd = new DefaultHttpCmd();
        httpCmd.setCommand(HttpCmdNames.HTTP_CMD_ADD_JOB);
        httpCmd.addParam("job", JSON.toJSONString(job));

        List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.JOB_TRACKER);
        if (CollectionUtils.isEmpty(jobTrackerNodeList)) {
            return new KVPair<Boolean, String>(false, I18nManager.getMessage("job.tracker.not.found"));
        }

        HttpCmdResponse response = null;
        for (Node node : jobTrackerNodeList) {
            httpCmd.setNodeIdentity(node.getIdentity());
            response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
            if (response.isSuccess()) {
                return new KVPair<Boolean, String>(true, "Add success");
            }
        }
        if (response != null) {
            return new KVPair<Boolean, String>(false, response.getMsg());
        } else {
            return new KVPair<Boolean, String>(false, "Add failed");
        }
    }

    @RequestMapping("/job-queue/executing-job-terminate")
    public RestfulResponse jobTerminate(String jobId) {
        RestfulResponse restfulResponse = new RestfulResponse();

        JobPo jobPo = appContext.getExecutingJobQueue().get(jobId);
        if (jobPo == null) {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg("该任务已经执行完成或者被删除");
            return restfulResponse;
        }

        String taskTrackerIdentity = jobPo.getTaskTrackerIdentity();

        Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(taskTrackerIdentity);
        if (node == null) {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg("执行该任务的TaskTracker已经离线");
            return restfulResponse;
        }

        HttpCmd cmd = new DefaultHttpCmd();
        cmd.setCommand(HttpCmdNames.HTTP_CMD_JOB_TERMINATE);
        cmd.setNodeIdentity(taskTrackerIdentity);
        cmd.addParam("jobId", jobId);
        HttpCmdResponse response = HttpCmdClient.doPost(node.getIp(), node.getHttpCmdPort(), cmd);
        if (response.isSuccess()) {
            restfulResponse.setSuccess(true);
        } else {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(response.getMsg());
        }

        return restfulResponse;
    }
}
