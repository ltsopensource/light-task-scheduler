package com.lts.tasktracker.jobdispatcher;

import com.lts.tasktracker.runner.JobRunner;
import org.junit.Test;

import java.util.Map;

/**
 * Created by hugui.hg on 3/2/16.
 */
public class JobRunnerScannerTest {

    @Test
    public void testScans() throws Exception {
        Map<String, JobRunner> map = new JobRunnerScanner().scans("com.lts.tasktracker.jobdispatcher");
        for (Map.Entry<String, JobRunner> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }
}