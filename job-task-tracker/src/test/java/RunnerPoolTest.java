import com.lts.job.core.domain.Job;
import com.lts.job.task.tracker.domain.Response;
import com.lts.job.task.tracker.expcetion.NoAvailableJobRunnerException;
import com.lts.job.task.tracker.runner.RunnerCallback;
import com.lts.job.task.tracker.runner.RunnerPool;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public class RunnerPoolTest {

    @Test
    public void test() {


        for (int i = 0; i < 18; i++) {

            try {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RunnerPool runnerPool = new RunnerPool();
                runnerPool.execute(new Job(), new RunnerCallback() {
                    @Override
                    public Job runComplete(Response response) {
                        return null;
                    }
                });
            } catch (NoAvailableJobRunnerException e) {
                e.printStackTrace();
            }
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
