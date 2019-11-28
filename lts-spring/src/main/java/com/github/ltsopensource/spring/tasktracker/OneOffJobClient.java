package com.github.ltsopensource.spring.tasktracker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.core.properties.TaskTrackerProperties;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.spring.quartz.QuartzLTSConfig.JobProperties;

/**
 * 一次性／临时性 任务客户端
 * @author huanghai
 * @date 2019/11/27
 */
@Service
@Validated
public class OneOffJobClient {
	@SuppressWarnings("rawtypes")
	@Autowired
	private JobClient jobClient;
	@Autowired
	private JobProperties jobProperties;
	@Autowired
	private JobClientProperties jobClientProperties;
	@Autowired
	private TaskTrackerProperties taskTrackerProperties;
	
	public void submitJob(@NotNull String shardValuePrefix
			, @NotNull String businessUniqeKey
			, @Future Date executeTime){
		submitJob(shardValuePrefix, businessUniqeKey, executeTime, new HashMap<String, String>());
	}
	
	public void submitJob(@NotNull String shardValuePrefix
				, @NotNull String businessUniqeKey
				, @Future Date executeTime
				, @NotNull Map<String,String> params){
        Job job = new Job();
        job.setTaskId(shardValuePrefix + "_" + businessUniqeKey);
        job.setRepeatCount(-1);
        job.setSubmitNodeGroup(jobClientProperties.getNodeGroup());
        job.setTaskTrackerNodeGroup(taskTrackerProperties.getNodeGroup());
        job.setTriggerDate(executeTime);
        job.setParam("description",  "OneOffJobClient#submitJob");
        for(Entry<String, String> each : params.entrySet()){
        		job.setParam(each.getKey(), each.getValue());
        }
        setJobProp(job);
        jobClient.submitJob(job);
	}
	
	private void setJobProp(Job job) {
		if (jobProperties == null) {
			return;
		}
		if (jobProperties.getMaxRetryTimes() != null) {
			job.setMaxRetryTimes(jobProperties.getMaxRetryTimes());
		}
		if (jobProperties.getNeedFeedback() != null) {
			job.setNeedFeedback(jobProperties.getNeedFeedback());
		}
		if (jobProperties.getRelyOnPrevCycle() != null) {
			job.setRelyOnPrevCycle(jobProperties.getRelyOnPrevCycle());
		}
		if (jobProperties.getReplaceOnExist() != null) {
			job.setReplaceOnExist(jobProperties.getReplaceOnExist());
		}
	}
}
