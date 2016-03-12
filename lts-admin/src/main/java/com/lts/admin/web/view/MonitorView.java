package com.lts.admin.web.view;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.admin.web.vo.NodeInfo;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Controller
public class MonitorView {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("monitor/tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);

        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));

        List<NodeInfo> taskTrackers = appContext.getBackendTaskTrackerMAccess().getTaskTrackers();

        model.addAttribute("taskTrackers", taskTrackers);

        return "monitor/tasktrackerMonitor";
    }

    @RequestMapping("monitor/jobtracker-monitor")
    public String jobTrackerMonitor(Model model) {

        List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.JOB_TRACKER);
        model.addAttribute("jobTrackerNodeGroups", taskTrackerNodeGroups);

        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));

        List<String> taskTrackers = appContext.getBackendJobTrackerMAccess().getJobTrackers();
        model.addAttribute("jobTrackers", taskTrackers);

        return "monitor/jobtrackerMonitor";
    }

    @RequestMapping("monitor/jobClient-monitor")
    public String jobClientMonitor(Model model) {

        List<NodeGroupPo> jobClientNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.JOB_CLIENT);
        model.addAttribute("jobClientNodeGroups", jobClientNodeGroups);

        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));

        List<NodeInfo> jobClients = appContext.getBackendJobClientMAccess().getJobClients();

        model.addAttribute("jobClients", jobClients);

        return "monitor/jobClientMonitor";
    }
}
