package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;
import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 8/7/16.
 */
public class JobPriorityBlockingDequeTest {

    @Test
    public void testOffer(){

        JobPriorityBlockingDeque deque = new JobPriorityBlockingDeque(300);

        for (int i = 0; i < 20; i++) {
            JobPo jobPo = new JobPo();
            jobPo.setJobId("21312" + (i%3));
            jobPo.setPriority(i);
            jobPo.setGmtModified(Long.valueOf(20-i));
            deque.offer(jobPo);
        }

        int size = deque.size();
        for (int i = 0; i < size; i++) {
            System.out.println(i + " : " + deque.pollLast());
        }
    }
}