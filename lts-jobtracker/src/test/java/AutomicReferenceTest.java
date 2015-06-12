import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 * <p/>
 * volatile是不能保证原子性的
 */
public class AutomicReferenceTest {

    private static volatile Integer num1 = 0;
    private static AtomicReference<Integer> ar = new AtomicReference<Integer>(num1);

    private static AtomicInteger atomicInteger = new AtomicInteger(1);

    @Test
    public void test_AtomicReference() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++)
                        while (true) {
                            Integer temp = ar.get();
                            if (ar.compareAndSet(temp, temp + 1)) break;
                        }
                }
            }).start();
        }
        Thread.sleep(10000);
        System.out.println(ar.get()); //10000000
    }

    @Test
    public void test_volatile() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        num1 = num1++;
                    }
                }
            }).start();
        }
        Thread.sleep(10000);
        System.out.println(num1); //something like 207183
    }


    @Test
    public void testInt() {

        System.out.println(atomicInteger.incrementAndGet());
        System.out.println(atomicInteger.getAndIncrement());

    }

    @Test
    public void testSync() {
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();
        new Thread(new Run()).start();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class Run implements Runnable {

        private static Map<String, Object> map  = new HashMap<String, Object>();

        @Override
        public void run() {
            synchronized (map) {
                System.out.println("ddd" + map);
                try {

                    map.put("ddd", UUID.randomUUID().toString());

                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
