import com.lts.job.client.JobClient;
import com.lts.job.client.RetryJobClient;
import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.common.cluster.Node;
import com.lts.job.common.domain.Job;
import com.lts.job.client.domain.Response;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.listener.MasterNodeChangeListener;
import com.lts.job.common.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobClientTest {

    private static int mode = 2;

    public static void main(String[] args) throws ParseException {

        final JobClient jobClient = new RetryJobClient();
//      final JobClient jobClient = new JobClient();
        jobClient.setNodeGroup("TEST");
//        jobClient.setClusterName("QN");
        jobClient.setZookeeperAddress("localhost:2181");
        // 任务重试保存地址，默认用户目录下
//        jobClient.setJobInfoSavePath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobFinishedHandler() {
            @Override
            public void handle(List<JobResult> jobResults) {
                // 任务执行反馈结果处理
                if (CollectionUtils.isNotEmpty(jobResults)) {
                    for (JobResult jobResult : jobResults) {
                        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 任务执行完成：" + jobResult);
                    }
                }
            }
        });
        jobClient.addMasterNodeChangeListener(new MasterListener());
        jobClient.start();

        Scanner scanner = new Scanner(System.in);

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
        while (!"quit".equals(input = scanner.next())) {
            try {
                if("now".equals(input)){
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

                if(mode == 1){
                    System.out.print("cronExpression模式:");
                }else if(mode == 2){
                    System.out.print("指定时间模式:");
                }

            } catch (Exception e) {
                System.out.println("输入错误");
            }
        }
        System.exit(0);
    }

    public static void submitWithCronExpression(final JobClient jobClient, String cronExpression) throws ParseException {
        Job job = new Job();
        job.setTaskId(UUID.randomUUID().toString());
        job.setParam("shopId", "111");
        job.setTaskTrackerNodeGroup("TEST_TRADE");
        job.setCronExpression(cronExpression);
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    public static void submitWithTrigger(final JobClient jobClient, String triggerTime) throws ParseException {
        Job job = new Job();
        job.setTaskId(UUID.randomUUID().toString());
        job.setParam("shopId", "111");
        job.setTaskTrackerNodeGroup("TEST_TRADE");
        if (triggerTime != null && !"".equals(triggerTime.trim())) {
            job.setTriggerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(triggerTime).getTime());
        }
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

}


class MasterListener implements MasterNodeChangeListener {

    /**
     * master 为 master节点
     * isMaster 表示当前节点是不是master节点
     *
     * @param master
     * @param isMaster
     */
    @Override
    public void change(Node master, boolean isMaster) {

        // 一个节点组master节点变化后的处理 , 譬如我多个JobClient， 但是有些事情只想只有一个节点能做。
    }
}
