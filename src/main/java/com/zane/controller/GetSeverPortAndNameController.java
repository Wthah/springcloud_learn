package com.zane.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetSeverPortAndNameController {
    @Value("${server.port}")
    private String port;
    @Value("${spring.application.name}")
    private String name;

    @GetMapping("/getInfo")
    public String getServerPortAndName(){
        return this.name + " : " + this.port;
    }
}
