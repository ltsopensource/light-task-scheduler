package com.lts.job.common.repository;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.store.mongo.AbstractMongoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 */
public class JobMongoRepository extends AbstractMongoRepository<JobPo> {


    /**
     * 得到要执行的job
     *
     * @param taskTackerGroup 执行的TaskTracker 的 nodeGroup
     * @param identity        执行的TaskTracker 的 identity
     * @return 返回要执行的Job(保证了只能被一个线程拿取到)
     */
    public JobPo getJobPo(String taskTackerGroup, String identity) {

        /**

         db.JobPo.findAndModify({
         query : {nodeGroup:'QN_TRADE', isRunning: },
         update : {$set : {isRunning:true}, $set:{taskTracker:'identity'}},
         sort : {priority:1,gmtCreate:1},
         new : true
         });

         */

        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("taskTrackerNodeGroup", taskTackerGroup);
        condition.put("isRunning", false);
        condition.put("isFinished", false);
        // 优先级升序,时间升序
        Query<JobPo> query = createQuery(condition, " priority, gmtCreate");

        UpdateOperations<JobPo> operations =
                ds.createUpdateOperations(JobPo.class)
                        .set("isRunning", true)
                        .set("taskTracker", identity)
                        .set("gmtModify", System.currentTimeMillis())
                        .set("remark", "");

        JobPo jobPo = ds.findAndModify(query, operations, false);

        return jobPo;
    }

    public List<JobPo> getAllJob() {
        return createQuery().asList();
    }


    /**
     * 设置 JOb 为 可运行状态
     *
     * @param jobPo
     */
    public void setJobRunnable(JobPo jobPo) {

        Query<JobPo> query = createQuery().field("jobId").equal(jobPo.getJobId()).field("isFinished").equal(false);

        UpdateOperations<JobPo> operations =
                ds.createUpdateOperations(JobPo.class)
                        .set("isRunning", false)
                        .set("taskTracker", "")
                        .set("gmtModify", System.currentTimeMillis())
                        .set("remark", jobPo.getRemark()  == null? "" : jobPo.getRemark())
                        .set("msg", "");

        ds.update(query, operations);
    }

    /**
     * 删除任务
     *
     * @param jobId
     */
    public JobPo findAndDeleteJob(String jobId) {
        Query<JobPo> query = createQuery().field("jobId").equal(jobId);

        return ds.findAndDelete(query);
    }

    public void delJob(String jobId) {
        Query<JobPo> query = createQuery().field("jobId").equal(jobId);
        ds.delete(query);
    }


    /**
     * 把任务标记为 已完成
     *
     * @param jobResults
     */
    public void finishedJob(List<JobResult> jobResults) {

        for (JobResult jobResult : jobResults) {

            Job job = jobResult.getJob();
            Query<JobPo> query = createQuery().field("jobId").equal(job.getJobId());

            UpdateOperations<JobPo> operations = null;
            operations = ds.createUpdateOperations(JobPo.class)
                    .set("isRunning", false)
                    .set("gmtModify", System.currentTimeMillis())
                    .set("success", jobResult.isSuccess())        // 执行成功还是失败
                    .set("isFinished", true)
                    .set("msg", jobResult.getMsg() == null ? "" : jobResult.getMsg())
                    .set("remark", "已经完成但是没有发送客户端成功，等待下次发送");
            ds.update(query, operations);
        }
    }

    /**
     * 根据最大死亡时间查询死亡的任务
     *
     * @param maxDeadTime
     * @return
     */
    public List<JobPo> getDeadJob(long maxDeadTime) {
        Query<JobPo> query = createQuery();
        query.field("taskTracker").notEqual("")
                .field("isFinished").equal(false)
                .field("isRunning").equal(true)
                .filter("gmtModify < ", System.currentTimeMillis() - maxDeadTime);

        return query.asList();
    }

    /**
     * 根据某个执行的节点得到 任务
     * @param identity
     * @return
     */
    public List<JobPo> getJobByTaskTracker(String identity){
        Query<JobPo> query = createQuery();
        query.field("taskTracker").equal(identity)
                .field("isFinished").equal(false)
                .field("isRunning").equal(true);
        return query.asList();
    }

    /**
     * 得到已经完成的任务
     *
     * @return
     */
    public List<JobPo> getFinishedJob() {
        Query<JobPo> query = createQuery();
        query.field("isFinished").equal(true);
        return query.asList();
    }

}
