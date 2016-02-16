package com.lts.startup;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hugui.hg on 2/16/16.
 */
public class TaskTrackerStartupTest {

    @Test
    public void testMain() throws Exception {
        TaskTrackerStartup.main(new String[]{"/Users/hugui/Data/Workspace/github/light-task-scheduler/lts-startup/lts-startup-tasktracker/src/main/resources/conf"});
    }
}