import com.lts.job.client.JobClient;
import com.lts.job.client.RetryJobClient;
import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.common.cluster.Node;
import com.lts.job.common.domain.Job;
import com.lts.job.client.domain.Response;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.listener.MasterNodeChangeListener;

import java.io.IOException;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobClientTest {

    public static void main(String[] args) {

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
            }
        });
        jobClient.addMasterNodeChangeListener(new MasterListener());
        jobClient.start();

        // 提交任务
        Job job = new Job();
        job.setTaskId(UUID.randomUUID().toString());
        job.setParam("shopId", "111");
        job.setTaskTrackerNodeGroup("TEST_TRADE");
//        job.setCronExpression("0 15 10 ? * 6L 2014-2016");
        Response response = jobClient.submitJob(job);
        System.out.println(response);

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Integer i = 0;
                while (true) {
                    try {
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Job job = new Job();
                        job.setTaskId((i++) + "_");
                        Map<String, String> extParams = new HashMap<String, String>();
                        extParams.put("key", "value");
                        job.setExtParams(extParams);
                        job.setTaskTrackerNodeGroup("TEST_TRADE");
                        Response response = jobClient.submitJob(job);
                        System.out.println(response);

                        if (i > 1000000) {
                            break;
                        }
                    }catch (Exception t){
                        t.printStackTrace();
                    }
                }

            }
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobClient.stop();
            }
        }));

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}


class MasterListener implements MasterNodeChangeListener {

    /**
     * master 为 master节点
     * isMaster 表示当前节点是不是master节点
     * @param master
     * @param isMaster
     */
    @Override
    public void change(Node master, boolean isMaster) {

        // 一个节点组master节点变化后的处理 , 譬如我多个JobClient， 但是有些事情只想只有一个节点能做。
    }
}
