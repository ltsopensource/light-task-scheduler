package com.lts.job.example.tasktracker;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.task.tracker.TaskTracker;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {
        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.setJobRunnerClass(TestJobRunner.class);

        taskTracker.setZookeeperAddress("localhost:2181");
        taskTracker.setNodeGroup("TEST_TRADE");
//        taskTracker.setClusterName("QN");
        taskTracker.setWorkThreads(20);
//        taskTracker.setJobInfoSavePath(Constants.USER_HOME);
        taskTracker.addMasterNodeChangeListener(new MasterListener());

        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class MasterListener implements MasterNodeChangeListener{

    /**
     * master 为 master节点
     * isMaster 表示当前节点是不是master节点
     * @param master
     * @param isMaster
     */
    @Override
    public void change(Node master, boolean isMaster) {

        // 一个节点组master节点变化后的处理
    }
}