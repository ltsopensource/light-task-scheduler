package com.lts.web.controller.ui;

import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.queue.domain.NodeGroupPo;
import com.lts.web.cluster.AdminAppContext;
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
    AdminAppContext appContext;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("node-manager")
    public String nodeManagerUI() {
        return "nodeManager";
    }

    @RequestMapping("node-group-manager")
    public String nodeGroupManagerUI() {
        return "nodeGroupManager";
    }

    @RequestMapping("node-onoffline-log")
    public String nodeOnOfflineLogUI(Model model) {
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(DateUtils.addDay(new Date(), -10)));
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(new Date()));
        return "nodeOnOfflineLog";
    }

    @RequestMapping("node-jvm-info")
    public String nodeJVMInfo(Model model, String identity) {
        model.addAttribute("identity", identity);
        return "nodeJvmInfo";
    }

    @RequestMapping("job-add")
    public String addJobUI(Model model) {
        setAttr(model);
        return "jobAdd";
    }

    @RequestMapping("job-logger")
    public String jobLoggerUI(Model model, String taskId, String taskTrackerNodeGroup,
                              Date startLogTime, Date endLogTime) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("taskTrackerNodeGroup", taskTrackerNodeGroup);
        if (startLogTime == null) {
            startLogTime = DateUtils.addMinute(new Date(), -10);
        }
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(startLogTime));
        if (endLogTime == null) {
            endLogTime = new Date();
        }
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(endLogTime));
        setAttr(model);
        return "jobLogger";
    }

    @RequestMapping("cron-job-queue")
    public String cronJobQueueUI(Model model) {
        setAttr(model);
        return "cronJobQueue";
    }

    @RequestMapping("executable-job-queue")
    public String executableJobQueueUI(Model model) {
        setAttr(model);
        return "executableJobQueue";
    }

    @RequestMapping("executing-job-queue")
    public String executingJobQueueUI(Model model) {
        setAttr(model);
        return "executingJobQueue";
    }

    @RequestMapping("load-job")
    public String loadJobUI(Model model) {
        setAttr(model);
        return "loadJob";
    }

    @RequestMapping("cron_generator_iframe")
    public String cronGeneratorIframe(Model model){
        return "cron/cronGenerator";
    }

    private void setAttr(Model model) {
        List<NodeGroupPo> jobClientNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.JOB_CLIENT);
        model.addAttribute("jobClientNodeGroups", jobClientNodeGroups);
        List<NodeGroupPo> taskTrackerNodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_TRACKER);
        model.addAttribute("taskTrackerNodeGroups", taskTrackerNodeGroups);
    }

}
