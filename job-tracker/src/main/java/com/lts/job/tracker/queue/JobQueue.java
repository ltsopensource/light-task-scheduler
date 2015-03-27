package com.lts.job.tracker.queue;

import java.util.List;

/**
 * 任务队列, 这个是java里面的BockingQueue不是一回事,
 * 这个只是对一个任务队列的抽象
 * @author Robert HG (254963746@qq.com) on 3/26/15.
 */
public interface JobQueue {

    /**
     * <pre>
     *   添加任务: 将任务添加到待执行队列
     * </pre>
     *
     * @param jobPo
     * @return
     */
    boolean add(JobPo jobPo) throws DuplicateJobException;

    /**
     * <pre>
     *    将任务:
     *      1. 按照 priority 和 triggerTime 排序, 取 triggerTime 小于当前时间的任务
     *      2. 取任务的时候还要将这个任务的 执行节点的Group名称 和 执行节点的标识 设置
     *      3. 这个必须操作必须是线程安全的原子操作
     *
     *      譬如mongo中可以采用 findAndModify 来实现
     *    这里的取任务，不是真正的将任务取走了，而且将任务状态改为了执行状态
     * </pre>
     *
     * @param taskTrackerGroup    执行节点的Group名称
     * @param taskTrackerIdentity 执行节点的标识
     * @return
     */
    JobPo take(String taskTrackerGroup, String taskTrackerIdentity);

    /**
     * <pre>
     *     重置任务，将任务重新设置为可执行状态
     * </pre>
     *
     * @param jobPo
     */
    void resume(JobPo jobPo);

    /**
     * <pre>
     *    移除任务 (如: 当任务同步完成之后)
     * </pre>
     *
     * @param jobId
     */
    void remove(String jobId);

    /**
     * <pre>
     *      根据执行时间的限制值去查询任务,
     *      譬如程序会自动检查 执行了一定时间的任务，看是否是死掉了，还是什么
     * </pre>
     *
     * @param limitExecTime
     */
    List<JobPo> getByLimitExecTime(long limitExecTime);

    /**
     * 得到分发到 某个TaskTracker上正在执行的任务
     *
     * @param taskTrackerIdentity
     * @return
     */
    List<JobPo> getRunningJob(String taskTrackerIdentity);

    /**
     * 更新cron任务的执行时间
     *
     * @param jobId
     * @param triggerTime
     */
    void updateScheduleTriggerTime(String jobId, Long triggerTime);

}
