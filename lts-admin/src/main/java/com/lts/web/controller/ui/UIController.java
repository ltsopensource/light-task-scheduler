package com.lts.web.controller.ui;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminApplication;
import com.lts.web.cluster.RegistryService;
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
    public String nodeManagerUI() {
        return "node-manager";
    }

    @RequestMapping("node/node-group-manager")
    public String nodeGroupManagerUI() {
        return "node-group-manager";
    }

    @RequestMapping("node/node-onoffline-log")
    public String nodeOnOfflineLogUI(Model model) {
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(DateUtils.addDay(new Date(), -10)));
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(new Date()));
        return "node-onoffline-log";
    }

    @RequestMapping("node/node-jvm-info")
    public String nodeJVMInfo(String identity) {
        return "node-jvm-info";
    }


    @RequestMapping("job-queue/job-add-ui")
    public String addJobUI(Model model) {
        setAttr(model);
        return "job-add";
    }

    @RequestMapping("job-logger/job-logger")
    public String jobLoggerUI(Model model, String taskId, String taskTrackerNodeGroup,
                              Date startLogTime, Date endLogTime) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("taskTrackerNodeGroup", taskTrackerNodeGroup);
        if (startLogTime == null) {
            startLogTime = DateUtils.addDay(new Date(), -3);
        }
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(startLogTime));
        if (endLogTime == null) {
            endLogTime = new Date();
        }
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(endLogTime));
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
