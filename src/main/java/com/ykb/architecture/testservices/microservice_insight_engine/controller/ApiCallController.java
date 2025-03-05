package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.dto.ApiCallResponse;
import com.ykb.architecture.testservices.microservice_insight_engine.service.ApiCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-calls")
public class ApiCallController {

    @Autowired
    private ApiCallService apiCallService;

    @PostMapping
    public ResponseEntity<ApiCallResponse> getApiCalls(@RequestParam (required = true) String consumer,@RequestParam (required = true) String provider) {
        return ResponseEntity.ok(apiCallService.getApiCalls(consumer,provider));
    }
} 