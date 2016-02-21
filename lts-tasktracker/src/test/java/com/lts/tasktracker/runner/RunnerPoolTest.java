package com.lts.tasktracker.runner;

import com.lts.core.cluster.Config;
import com.lts.core.cluster.LTSConfig;
import com.lts.core.constant.Environment;
import com.lts.core.domain.Job;
import com.lts.core.domain.JobWrapper;
import com.lts.core.json.JSON;
import com.lts.ec.injvm.InjvmEventCenter;
import com.lts.tasktracker.domain.Response;
import com.lts.tasktracker.domain.TaskTrackerAppContext;
import com.lts.tasktracker.expcetion.NoAvailableJobRunnerException;
import com.lts.tasktracker.monitor.TaskTrackerMonitor;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public class RunnerPoolTest {

    @Test
    public void testInterruptor() throws NoAvailableJobRunnerException {

        LTSConfig.setEnvironment(Environment.UNIT_TEST);

        Config config = new Config();
        config.setWorkThreads(10);
        config.setIdentity("fjdaslfjlasj");

        TaskTrackerAppContext appContext = new TaskTrackerAppContext();
        appContext.setConfig(config);
        appContext.setEventCenter(new InjvmEventCenter());
//        appContext.setJobRunnerClass(TestInterruptorJobRunner.class);
        appContext.setJobRunnerClass(NormalJobRunner.class);

        RunnerPool runnerPool = new RunnerPool(appContext);

        appContext.setRunnerPool(runnerPool);

        TaskTrackerMonitor monitor = new TaskTrackerMonitor(appContext);
        appContext.setMonitor(monitor);

        RunnerCallback callback = new RunnerCallback(){

            @Override
            public JobWrapper runComplete(Response response) {
                System.out.println("complete:" + JSON.toJSONString(response));
                return null;
            }
        };

        Job job = new Job();
        job.setTaskId("fdsafas");

        JobWrapper jobWrapper = new JobWrapper();
        jobWrapper.setJobId("111111");
        jobWrapper.setJob(job);

        runnerPool.execute(jobWrapper, callback);


        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 5s之后停止
        runnerPool.stopWorking();
    }

}