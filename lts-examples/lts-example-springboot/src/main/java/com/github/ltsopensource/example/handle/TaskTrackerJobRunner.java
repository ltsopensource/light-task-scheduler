package com.github.ltsopensource.example.handle;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.example.service.JobService;
import com.github.ltsopensource.example.service.pojo.RealTimeJob;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

@Component
public class TaskTrackerJobRunner implements JobRunner {
    private static final Logger logger = LoggerFactory.getLogger(TaskTrackerJobRunner.class);
    @Autowired
	private JobService jobService;
	@Autowired
	private MetricRegistry regist;
	
    @Override
    public Result run(JobContext jctx) throws Throwable {
    	String result = "";
    	Date today = new Date();
		String datetime = DateUtils.formatDate(today, "yyyy-MM-dd HH:mm:ss");
        try {
//            BizLogger log = LtsLoggerFactory.getBizLogger();
//            BizLogger log = jctx.getBizLogger();
            Job job = jctx.getJob();
            // TODO 业务逻辑
            logger.info("--lts-context:" + jctx);
            // 会发送到 LTS (JobTracker上)
//            log.info("LTS-JOB, Insert Job.");
            System.out.println("\n\n\n---------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("-job-task-tracker--"+job.getTaskId()+"-->"+job.getTaskTrackerNodeGroup()+"-->"+job.getSubmitNodeGroup());
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------\n\n\n");
            String table = job.getParam("table");
            int type = Integer.valueOf(job.getParam("type"));
            if(type==1){
            	result = selectJob(table);
            }else{
            	result = insertJob(table, today);
            }
        } catch (Exception e) {
        	logger.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "["+datetime+"]"+result);
    }
    
    private String insertJob(String table,Date today){
    	String result = "";
    	final Meter meter = regist.meter("LTS-Insert-Handle-Metric-TPS");
		final Histogram histogram = regist.histogram("LTS-Insert-Handle-Metric-Histogram");
    	final Counter jobcounter = regist.counter("LTS-Insert-Handle-Metric-Counter");
    	final Timer jobtimer = regist.timer("LTS-Insert-Handle-Metric-ExecuteTime");
		int random = new Random().nextInt(10000);
		RealTimeJob obj = new RealTimeJob();
		obj.setCode("code");
		obj.setContent("百度翻译支持全球28种热门语言互译");
		obj.setCreatetime(today);
		obj.setFlag(random%2==0?true:false);
		obj.setMax_retry_times(random);
		obj.setMsg(random%2==0?"Success":"Error");
		obj.setName(table);
		obj.setPrice(3.1413926+random);
		obj.setPriority(0);
		obj.setRetry_times(1);
		obj.setStatus(random%2);
		obj.setTotal(today.getTime());
		obj.setUpdatetime(today);
		meter.mark();
		jobcounter.inc();
		histogram.update(new Random().nextInt(10));
        final Timer.Context context = jobtimer.time();
		try {
			jobService.insert(table, obj);
			result = "insert success!";
		}catch (Exception e) {
			logger.info("--lts insert error!", e);
			result = "insert error!";
		}finally {
            context.stop();
        }
		return result;
    }
    private String selectJob(String table){
    	String result = "";
    	Meter meter = regist.meter("LTS-Select-Handle-Metric-TPS");
		Histogram histogram = regist.histogram("LTS-Select-Handle-Metric-Histogram");
    	Counter jobcounter = regist.counter("LTS-Select-Handle-Metric-Counter");
    	Timer jobtimer = regist.timer("LTS-Select-Handle-Metric-ExecuteTime");
		int count = 0;
		meter.mark();
		jobcounter.inc();
		histogram.update(new Random().nextInt(10));
        final Timer.Context context = jobtimer.time();
		try {
			List<RealTimeJob> list = jobService.select(table);
			count = list!=null?list.size():0;
			result = "select data size:"+count;
		}catch (Exception e) {
			logger.info("--lts select error!", e);
			result = "select error!";
		}finally {
            context.stop();
        }
		return result;
    }
}
