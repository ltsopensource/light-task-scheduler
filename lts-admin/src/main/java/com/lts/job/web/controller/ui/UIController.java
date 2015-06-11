package com.lts.job.web.controller.ui;

import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.commons.utils.DateUtils;
import com.lts.job.queue.domain.NodeGroupPo;
import com.lts.job.web.cluster.AdminApplication;
import com.lts.job.web.cluster.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
@Controller
public class UIController {

    @Autowired
    RegistryService registryService;
    @Autowired
    AdminApplication application;

    @RequestMapping("node/node-manager")
    public String nodeManagerUI(Model model) {
        List<String> clusterNames = registryService.getAllClusterNames();
        model.addAttribute("clusterNames", clusterNames);
        return "node-manager";
    }

    @RequestMapping("job-queue/job-add-ui")
    public String addJobUI(Model model) {
        setAttr(model);
        return "job-add";
    }

    @RequestMapping("job-logger/job-logger")
    public String jobLoggerUI(Model model, String taskId, String taskTrackerNodeGroup,
                              Date startTimestamp, Date endTimestamp) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("taskTrackerNodeGroup", taskTrackerNodeGroup);
        if (startTimestamp == null) {
            startTimestamp = DateUtils.addDay(new Date(), -3);
        }
        model.addAttribute("startTimestamp", DateUtils.formatYMD_HMS(startTimestamp));
        if (endTimestamp == null) {
            endTimestamp = new Date();
        }
        model.addAttribute("endTimestamp", DateUtils.formatYMD_HMS(endTimestamp));
        setAttr(model);
        return "job-logger";
    }

    @RequestMapping("job-queue/cron-job-queue")
    public String cronJobQueueUI(Model model) {
        setAttr(model);
        return "cron-job-queue";
    }

    @RequestMapping("job-queue/executable-job-queue")
    public String executableJobQueueUI(Model model) {
        setAttr(model);
        return "executable-job-queue";
    }

    @RequestMapping("job-queue/executing-job-queue")
    public String executingJobQueueUI(Model model) {
        setAttr(model);
        return "executing-job-queue";
    }

    private void setAttr(Model model) {
        List<NodeGroupPo> jobClientNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.JOB_CLIENT);
        model.addAttribute("jobClientNodeGroups", jobClientNodeGroups);
        List<NodeGroupPo> taskTrackerNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);
    }

}
