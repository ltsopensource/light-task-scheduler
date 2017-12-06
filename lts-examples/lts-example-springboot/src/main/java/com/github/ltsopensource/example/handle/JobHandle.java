package com.github.ltsopensource.example.handle;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.domain.Response;
@Component
public class JobHandle {
	public static String TASK_TRACKER_NODE_GROUP = "css-task-tracker";
	@SuppressWarnings("rawtypes")
	@Autowired
	private JobClient jobClient;
	/**
	 * @decription 实时任务
	 * @author yi.zhang
	 * @time 2017年11月3日 上午11:23:15
	 * @param url		请求URL
	 * @param params	请求参数
	 * @return
	 */
	public Response handleRealtimeJob(int type,String table,String taskId){
		Job job = new Job();
        job.setTaskId("realtime_"+taskId);
        job.setParam("type", type+"");
        job.setParam("table", table);
        job.setTaskTrackerNodeGroup(TASK_TRACKER_NODE_GROUP);
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        Response response = jobClient.submitJob(job);
        return response;
	}
	/**
	 * @decription 重复任务
	 * @author yi.zhang
	 * @time 2017年11月3日 上午11:24:11
	 * @param url			请求URL
	 * @param params		请求参数
	 * @param repeatCount	重复次数
	 * @param intervalTime	重复间隔(单位:秒)
	 * @return
	 */
	public Response handleRepeatJob(int type,String table,String taskId,int repeatCount,long intervalTime){
		Job job = new Job();
        job.setTaskId("repeat_"+taskId);
        job.setParam("type", type+"");
        job.setParam("table", table);
        job.setTaskTrackerNodeGroup(TASK_TRACKER_NODE_GROUP);
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setRepeatCount(repeatCount);             // 一共执行50次
        job.setRepeatInterval(intervalTime * 1000L);  // 50s 执行一次
        Response response = jobClient.submitJob(job);
		return response;
	}
	/**
	 * @decription 周期任务
	 * @author yi.zhang
	 * @time 2017年11月3日 上午11:25:25
	 * @param url		请求URL
	 * @param params	请求参数
	 * @param cron		周期表达式(例如:0 0/1 * * * ?)
	 * @return
	 */
	public Response handleCronJob(int type,String table,String taskId,String cron){
		Job job = new Job();
        job.setTaskId("cron"+taskId);
        job.setParam("type", type+"");
        job.setParam("table", table);
        job.setTaskTrackerNodeGroup(TASK_TRACKER_NODE_GROUP);     // 执行要执行该任务的taskTracker的节点组名称
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression("0 0/1 * * * ?");
        Response response = jobClient.submitJob(job);
		return response;
	}
	/**
	 * @decription 定时任务
	 * @author yi.zhang
	 * @time 2017年11月3日 上午11:26:43
	 * @param url		请求URL
	 * @param params	请求参数
	 * @param start		开始时间
	 * @param time		定时时间(单位:秒)
	 * @return
	 */
	public Response handleTriggerTimeJob(int type,String table,String taskId,Date start,long time){
		Job job = new Job();
        job.setTaskId("trigger_"+taskId);
        job.setParam("type", type+"");
        job.setParam("table", table);
        job.setTaskTrackerNodeGroup(TASK_TRACKER_NODE_GROUP);
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        if(start==null){
        	start = new Date();
        }
        job.setTriggerTime(start.getTime()+time * 1000L);   // 1 小时之后执行
        Response response = jobClient.submitJob(job);
		return response;
	}
}