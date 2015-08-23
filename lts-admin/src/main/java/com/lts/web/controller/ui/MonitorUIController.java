package com.lts.web.controller.ui;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminApplication;
import com.lts.web.repository.TaskTrackerMIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Controller
public class MonitorUIController {

    @Autowired
    AdminApplication application;
    @Autowired
    TaskTrackerMIRepository repository;

    @RequestMapping("monitor/tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);

        model.addAttribute("startTime", DateUtils.formatYMD(new Date()));
        model.addAttribute("endTime", DateUtils.formatYMD(new Date()));

        Map<String, List<String>> taskTrackerMap = repository.getTaskTrackerMap();
        model.addAttribute("taskTrackerMap", taskTrackerMap);

        return "tasktracker-monitor";
    }

}
