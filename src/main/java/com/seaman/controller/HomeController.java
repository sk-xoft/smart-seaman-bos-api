package com.seaman.controller;

import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String index() {
        return AppSys.APPLICATION_NAME;
    }

    @GetMapping(Routes.HEALTH)
    public String health(){
        // TODO Process health check is api online.
        return "Success";
    }

//    @PostMapping("/post")
//    public String postTest(@RequestBody PostRequest request) {
//        return "Success Post";
//    }

}

