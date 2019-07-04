package com.github.ltsopensource.admin.web.view;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Controller
public class MonitorView {

    @Autowired
    ApplicationContext context;

    @RequestMapping("monitor/jobtracker-monitor")
    public String jobTrackerMonitor(Model model) {

        initTimeRange(model);

        // FIXME find where to get it
        List<String> taskTrackers = null;
        model.addAttribute("jobTrackers", taskTrackers);

        return "monitor/jobtrackerMonitor";
    }

    @RequestMapping("monitor/tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        initTimeRange(model);

        return "monitor/tasktrackerMonitor";
    }

    @RequestMapping("monitor/jobClient-monitor")
    public String jobClientMonitor(Model model) {

        initTimeRange(model);

        return "monitor/jobClientMonitor";
    }

    private void initTimeRange(Model model) {
        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.addHours(endDate, -3));
        model.addAttribute("endTime", endDate);
    }
}
