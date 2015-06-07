package com.lts.job.web.controller.ui;

import com.lts.job.core.cluster.NodeType;
import com.lts.job.queue.domain.NodeGroupPo;
import com.lts.job.web.cluster.AdminApplication;
import com.lts.job.web.cluster.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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

    private void setAttr(Model model){
        List<NodeGroupPo> jobClientNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.JOB_CLIENT);
        model.addAttribute("jobClientNodeGroups", jobClientNodeGroups);
        List<NodeGroupPo> taskTrackerNodeGroups = application.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);
    }
}
