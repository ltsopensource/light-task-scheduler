package com.lts.web.controller.ui;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminAppContext;
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
    private AdminAppContext appContext;
    @Autowired
    private TaskTrackerMonitorRepo taskTrackerMonitorRepo;
    @Autowired
    private JobTrackerMonitorRepo jobTrackerMonitorRepo;

    @RequestMapping("tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);

        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));

        List<Map<String, String>> taskTrackerMap = taskTrackerMonitorRepo.getTaskTrackerMap();

        Map<String, String> map = new HashMap<String, String>();
        if(CollectionUtils.isNotEmpty(taskTrackerMap)){
            for (Map<String, String> pairMap : taskTrackerMap) {
                map.put(pairMap.get("key"), pairMap.get("value"));
            }
        }

        model.addAttribute("taskTrackerMap", map);

        return "tasktrackerMonitor";
    }

    @RequestMapping("jobtracker-monitor")
    public String jobTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.JOB_TRACKER);
        model.addAttribute("jobTrackerNodeGroups", taskTrackerNodeGroups);

        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));

        List<String> taskTrackers = jobTrackerMonitorRepo.getJobTrackers();
        model.addAttribute("jobTrackers", taskTrackers);

        return "jobtrackerMonitor";
    }

}
