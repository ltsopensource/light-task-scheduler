package com.lts.job.example.support;

import com.lts.job.client.JobClient;
import com.lts.job.client.domain.Response;
import com.lts.job.core.domain.Job;
import com.lts.job.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class BaseJobClientTest {

    private static int mode = 2;

    protected JobClient jobClient;

    public void startConsole() throws IOException {

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

    public void submitWithCronExpression(final JobClient jobClient, String cronExpression) throws ParseException {
        Job job = new Job();
        job.setTaskId(StringUtils.generateUUID());
        job.setParam("shopId", "111");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setCronExpression(cronExpression);
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    public void submitWithTrigger(final JobClient jobClient, String triggerTime) throws ParseException {
        Job job = new Job();
        job.setTaskId(StringUtils.generateUUID());
        job.setParam("shopId", "111");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        if (triggerTime != null && !"".equals(triggerTime.trim())) {
            job.setTriggerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(triggerTime).getTime());
        }
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }
}
