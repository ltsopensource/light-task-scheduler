package com.github.ltsopensource.tasktracker.interrupter;

import sun.nio.ch.Interruptible;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

abstract class InterruptSupport {
    private volatile boolean interrupted = false;
    private Interruptible interruptor = new InterruptRead.InterruptibleAdapter() {

        public void interrupt() {
            interrupted = true;
            InterruptSupport.this.interrupt(); // 位置3
        }
    };

    public final boolean execute() throws InterruptedException {
        try {
            blockedOn(interruptor); // 位置1
            System.out.println("=======1");
            if (Thread.currentThread().isInterrupted()) { // 立马被interrupted
                ((InterruptRead.InterruptibleAdapter)interruptor).interrupt();
                System.out.println("=======2");
            }
            // 执行业务代码
            bussiness();
            System.out.println("=======3");
        } finally {
            blockedOn(null); // 位置2
            System.out.println("=======4");
        }
        return interrupted;
    }

    public abstract void bussiness();

    public abstract void interrupt();

    // -- sun.misc.SharedSecrets --
    static void blockedOn(Interruptible intr) { // package-private
        jdk.internal.misc.SharedSecrets.getJavaLangAccess().blockedOn(intr);
    }
}

public class InterruptRead extends InterruptSupport {
    private FileInputStream in;

    @Override
    public void bussiness() {
        File file = new File("/dev/urandom"); // 读取linux黑洞，永远读不完
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            while (in.read(bytes, 0, 1024) > 0) {
                //
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FileInputStream getIn() {
        return in;
    }

    @Override
    public void interrupt() {
        try {
            in.getChannel().close();
            System.out.println("=======6");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        final InterruptRead test = new InterruptRead();
        Thread t = new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    System.out.println("InterruptRead start!");
                    test.execute();
                } catch (InterruptedException e) {
                    System.out.println("InterruptRead end! cost time : " + (System.currentTimeMillis() - start));
                    e.printStackTrace();
                }
            }
        };
        t.start();
        // 先让Read执行3秒
        Thread.sleep(30000);
        // 发出interrupt中断
//        t.interrupt();
    }

    public static abstract class InterruptibleAdapter implements Interruptible{
        public void interrupt(Thread thread) {
            interrupt();
        }

        public abstract void interrupt();
    }
}
