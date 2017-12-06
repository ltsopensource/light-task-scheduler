package com.github.ltsopensource.example.java;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.exception.JobSubmitException;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.jobclient.domain.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by hugui.hg on 4/23/16.
 */
public class TestSubmit {

    static JobClient jobClient;

    public static void main(String[] args) throws IOException {
        // 方式2
        jobClient = new JobClientBuilder()
                .setPropertiesConfigure("lts.properties")
                .setJobCompletedHandler(new JobCompletedHandlerImpl())
                .build();

        jobClient.start();

        startConsole();
    }

    private static int mode = 2;

    public static void startConsole() throws IOException {

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        String help = "命令参数: \n" +
                "\t1:cronExpression模式,如 0 0/1 * * * ?(一分钟执行一次), \n\t2:指定时间模式 yyyy-MM-dd HH:mm:ss,在执行时间模式下，如果字符串now，表示立即执行 \n" +
                "\tquit:退出\n" +
                "\thelp:帮助";
        System.out.println(help);
        System.out.println("指定时间模式:");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobClient.stop();
            }
        }));

        String input;
        while (!"quit".equals(input = buffer.readLine())) {
            try {
                if ("now".equals(input)) {
                    input = "";
                }
                if ("help".equals(input)) {
                    System.out.println(help);
                } else if ("1".equals(input)) {
                    mode = 1;
                } else if ("2".equals(input)) {
                    mode = 2;
                } else {
                    if (mode == 1) {
                        submitWithCronExpression(jobClient, input);
                    } else if (mode == 2) {
                        submitWithTrigger(jobClient, input);
                    }
                }

                if (mode == 1) {
                    System.out.print("cronExpression模式:");
                } else if (mode == 2) {
                    System.out.print("指定时间模式:");
                }

            } catch (Exception e) {
                System.out.println("输入错误");
            }
        }
        System.exit(0);
    }

    public static void submitWithCronExpression(final JobClient jobClient, String cronExpression) throws ParseException, JobSubmitException {
        Job job = new Job();
        // 必填，尽量taskId 有一定规律性，能让自己识别
        job.setTaskId(StringUtils.generateUUID());
        // 任务的参数，value必须为字符串
        job.setParam("shopId", "111");
        // 执行节点的group名称
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        // 是否接收执行反馈消息 jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl()); 中接受
        job.setNeedFeedback(true);
        // 这个是 cron expression 和 quartz 一样，可选
        job.setCronExpression(cronExpression);
        // 这个是指定执行时间，可选
        // job.setTriggerTime(new Date());
        // 当 cronExpression 和 triggerTime 都不设置的时候，默认是立即执行任务
        // response 返回提交成功还是失败
        Response response = jobClient.submitJob(job);


        System.out.println(response);
    }

    public static void submitWithTrigger(final JobClient jobClient, String triggerTime) throws ParseException, JobSubmitException {
        Job job = new Job();
        job.setTaskId(StringUtils.generateUUID());
        job.setParam("shopId", "111");
        job.setMaxRetryTimes(5);
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        if (triggerTime != null && !"".equals(triggerTime.trim())) {
            job.setTriggerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(triggerTime).getTime());
        }
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }
}
