package io.grpc.grpcswagger.controller;

import io.grpc.grpcswagger.manager.ServiceConfigManager;
import io.grpc.grpcswagger.model.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private ServiceConfigManager serviceConfigManager;

    @GetMapping("/")
    public String index(Model model) {

        List<ServiceConfig> serviceConfigs = serviceConfigManager.getServiceConfigs();

        model.addAttribute("serviceConfigs", serviceConfigs);

        return "index";
    }


}
