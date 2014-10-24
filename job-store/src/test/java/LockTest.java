import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 */
public class LockTest {


    private static final Lock lock = new ReentrantLock();

    @Test
    public void testReentrantLock() throws IOException {

        new Thread(new LockRunner("11")).start();
        new Thread(new LockRunner("22")).start();

        System.in.read();
    }

    @Test
    public void testSynchronized() throws IOException {

        new Thread(new SyncRunner("11")).start();
        new Thread(new SyncRunner("22")).start();

        System.in.read();

    }

    private static Object object = new Object();

    class SyncRunner implements Runnable{

        private String name;

        SyncRunner(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            synchronized (object){
                System.out.println("name=" + name);
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }




    class LockRunner implements Runnable {

        private String name;

        private int index;
        LockRunner(String name) {
            this.name = name;
        }

        @Override
        public void run() {

            lock.lock();
            try {
                for (int i = 0; i < 5; i++) {
                    System.out.println("name=" + name + ",i=" + i + ",index=" + index);
                    if(index > 2){
                        break;
                    }
                    if(i == 2){
                        index ++;
                        run();
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }

        }
    }
}
