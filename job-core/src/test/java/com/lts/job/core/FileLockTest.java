package com.lts.job.core;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class FileLockTest {

    @Test
    public void test() {

        Thread_readFile thf3=new Thread_readFile();
        Thread_readFile thf4=new Thread_readFile();
        thf3.start();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thf4.start();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

class Thread_readFile extends Thread {

    public void run() {
        try {
            Long start = System.currentTimeMillis();
            File file = new File("/Users/hugui/.dubbo/dubbo-registry-localhost.cache");

            //给该文件加锁
            RandomAccessFile fis = new RandomAccessFile(file, "rw");
            FileChannel channel = fis.getChannel();
            FileLock flin = null;
            while (true) {
                try {
                    flin = channel.tryLock();
                    if(flin == null){
                        throw new RuntimeException("获取锁失败");
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("有其他线程正在操作该文件，当前线程休眠1000毫秒");
                    sleep(1000);
                }

            }
            byte[] buf = new byte[1024];
            StringBuffer sb = new StringBuffer();
            while ((fis.read(buf)) != -1) {
                sb.append(new String(buf, "utf-8"));
                buf = new byte[1024];
            }

            System.out.println(sb.toString());
            // debug 住 , 可以看其他jvm的表现
            flin.release();
            channel.close();
            fis.close();
            System.out.println("读文件共花了" + (System.currentTimeMillis() - start) + " ms ");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}