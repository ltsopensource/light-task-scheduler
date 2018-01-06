package com.github.ltsopensource.example.controller;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.ltsopensource.example.handle.JobHandle;
import com.github.ltsopensource.example.service.JobService;

@Controller
@EnableAutoConfiguration
public class JobController {
	private Logger logger = LoggerFactory.getLogger(JobController.class);
    @Autowired
    private JobHandle handle;
    @Autowired
	private JobService jobService;
	@Autowired
	private MetricRegistry regist;
    @RequestMapping("/")
    @ResponseBody
    public String index(@RequestParam(defaultValue="0") int type) {
    	JobService.DATABASE_TYPE = type;
        return "[DBType:"+(type==1?"MongoDB":"MySQL|MariaDB")+"]";
    }
    @RequestMapping("/job")
    @ResponseBody
    public String job(@RequestParam(defaultValue="0") int type,@RequestParam(defaultValue="1") int thread,@RequestParam(defaultValue="1") int count,@RequestParam(defaultValue="false") boolean flag,@RequestParam(defaultValue="1") int jobType,@RequestParam(defaultValue="") String childKey) {
    	String table = "insert_job";
    	boolean rebuilt = false;
    	if(type==1){
    		table = "select_job";
    	}else if(type==2){
    		table = "update_job";
    	}else if(type==3){
    		table = "delete_job";
    	}else{
    		rebuilt = true;
    	}
    	jobService.createTable(table, rebuilt);
    	final int _type = type;
    	final int _thread = thread;
    	final String _table = table;
    	final int _count = count;
    	final boolean _flag = flag;
    	final int _jobType = jobType;
    	Thread main = new Thread(new Runnable() {
			@Override
			public void run() {
				if(_flag){
					jobthread(_type, _thread, _table, _count, _jobType);
				}else{
					jobdetail(_type, _thread, _table, _count, _jobType);
				}
			}
		});
    	main.start();
        return "["+table+"]Success!";
    }
    private void jobdetail(final int type,final int thread,final String table,final int count,final int jobType){
    	final Meter meter = regist.meter("Controller-Metric-TPS");
    	final Histogram histogram = regist.histogram("Controller-Metric-Histogram");
    	final Counter jobcounter = regist.counter("Controller-Metric-Counter");
    	final Timer jobtimer = regist.timer("Controller-Metric-ExecuteTime");
    	ExecutorService service = Executors.newSingleThreadExecutor();
    	if(thread>1){
    		service = Executors.newFixedThreadPool(thread);
    	}else{
    		if(thread<0){
    			service = Executors.newCachedThreadPool();
    		}
    	}
    	for(int i=0;thread>0?i<thread:true;i++){
    		final int thread_id = i;
    		service.submit(new Runnable() {
    			@Override
    			public void run() {
    				for(long i=0;count>0?i<count:true;i++){
    					String today = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    					meter.mark();
    					jobcounter.inc();
    					histogram.update(i);
    			        final Timer.Context context = jobtimer.time();
    					try {
    						if(jobType==0){
        						String taskId = "cycle_"+table+"_"+thread_id+"_"+i+"_"+today;
        						handle.handleCronJob(type, table, taskId, "0 0/1 * * * ?");
        					}else{
        						String taskId = "single_"+table+"_"+thread_id+"_"+i+"_"+today;
        						handle.handleRealtimeJob(type, table, taskId);
        					}
    					} catch (Exception e) {
    						logger.error("--lts handle error",e);
    					}finally {
    			            context.stop();
    			        }
    				}
    			}
    		});
    	}
    }
    private void jobthread(final int type,final int thread,final String table,final int count,final int jobType){
    	final Meter meter = regist.meter("Controller-Metric-TPS");
    	final Histogram histogram = regist.histogram("Controller-Metric-Histogram");
    	final Counter jobcounter = regist.counter("Controller-Metric-Counter");
    	final Timer jobtimer = regist.timer("Controller-Metric-ExecuteTime");
    	for(int i=0;thread>0?i<thread:true;i++){
    		final int thread_id = i;
    		String thread_name = "["+table+"_Thread_"+thread_id+"]";
    		Thread service = new Thread(new Runnable() {
    			@Override
    			public void run() {
    				for(long i=0;count>0?i<count:true;i++){
    					String today = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    					meter.mark();
    					jobcounter.inc();
    					histogram.update(i);
    			        final Timer.Context context = jobtimer.time();
    					try {
    						if(jobType==0){
        						String taskId = "cycle_"+table+"_"+thread_id+"_"+i+"_"+today;
        						handle.handleCronJob(type, table, taskId, "0 0/1 * * * ?");
        					}else{
        						String taskId = "tsingle_"+table+"_"+thread_id+"_"+i+"_"+today;
        						handle.handleRealtimeJob(type, table, taskId);
        					}
    					} catch (Exception e) {
    						logger.error("--lts handle error",e);
    					}finally {
    			            context.stop();
    			        }
    				}
    			}
    		});
    		service.setName(thread_name);
    		service.start();
    	}
    }
}
