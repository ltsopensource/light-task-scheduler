import com.lts.job.common.domain.Job;
import com.lts.job.task.tracker.runner.JobRunner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TestJobRunner implements JobRunner {

    private static AtomicInteger i = new AtomicInteger(0);

    @Override
    public void run(Job job) throws Throwable {

        System.out.println("我要执行"+ job);
        System.out.println(job.getParam("shopId"));

        try {
            Thread.sleep(2*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
