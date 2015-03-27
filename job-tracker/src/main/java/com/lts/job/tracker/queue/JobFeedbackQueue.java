package com.lts.job.tracker.queue;


import java.util.List;

/**
 * 这个是用来保存反馈客户端失败的任务结果队列
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public interface JobFeedbackQueue {

    /**
     * 添加反馈的任务结果
     * @param jobFeedbackPo
     */
    public void add(List<JobFeedbackPo> jobFeedbackPo);

    /**
     * 删除记录
     * @param id
     */
    public void remove(String id);

    /**
     * 反馈队列中有多少个
     * @return
     */
    public long count();

    /**
     * 分页查询
     * @param offset
     * @param limit
     * @return
     */
    public List<JobFeedbackPo> fetch(int offset, int limit);

}
