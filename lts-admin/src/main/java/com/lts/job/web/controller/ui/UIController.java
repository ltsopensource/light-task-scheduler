package com.lts.job.web.controller.ui;

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

    @RequestMapping("node/node-manager")
    public String nodeManagerUI(Model model) {
        List<String> clusterNames = registryService.getAllClusterNames();
        model.addAttribute("clusterNames", clusterNames);
        return "node-manager";
    }

}
