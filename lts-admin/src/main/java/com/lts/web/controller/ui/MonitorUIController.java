package com.lts.web.controller.ui;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminApplication;
import com.lts.web.repository.mapper.JobTrackerMonitorRepo;
import com.lts.web.repository.mapper.TaskTrackerMonitorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Controller
public class MonitorUIController {

    @Autowired
    private AdminApplication application;
    @Autowired
    private TaskTrackerMonitorRepo taskTrackerMonitorRepo;
    @Autowired
    private JobTrackerMonitorRepo jobTrackerMonitorRepo;

    @RequestMapping("monitor/tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);

        model.addAttribute("startTime", DateUtils.formatYMD(new Date()));
        model.addAttribute("endTime", DateUtils.formatYMD(new Date()));

        List<Map<String, String>> taskTrackerMap = taskTrackerMonitorRepo.getTaskTrackerMap();

        Map<String, String> map = new HashMap<String, String>();
        if(CollectionUtils.isNotEmpty(taskTrackerMap)){
            for (Map<String, String> pairMap : taskTrackerMap) {
                map.put(pairMap.get("key"), pairMap.get("value"));
            }
        }

        model.addAttribute("taskTrackerMap", map);

        return "tasktracker-monitor";
    }

    @RequestMapping("monitor/jobtracker-monitor")
    public String jobTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.JOB_TRACKER);
        model.addAttribute("jobTrackerNodeGroups", taskTrackerNodeGroups);

        model.addAttribute("startTime", DateUtils.formatYMD(new Date()));
        model.addAttribute("endTime", DateUtils.formatYMD(new Date()));

        List<String> taskTrackers = jobTrackerMonitorRepo.getJobTrackers();
        model.addAttribute("jobTrackers", taskTrackers);

        return "jobtracker-monitor";
    }

}
