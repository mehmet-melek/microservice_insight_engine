package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.model.ApplicationInfo;
import com.ykb.architecture.testservices.microservice_insight_engine.service.ApplicationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationInfoController {

    @Autowired
    private ApplicationInfoService applicationInfoService;

    @GetMapping
    public ResponseEntity<List<ApplicationInfo>> getAllApplications(
            @RequestParam(required = false, defaultValue = "false") boolean isNgbaOnly) {
        return ResponseEntity.ok(applicationInfoService.getAllApplications(isNgbaOnly));
    }
} 