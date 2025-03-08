package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.ApplicationEdge;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.Edge;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.GraphData;
import com.ykb.architecture.testservices.microservice_insight_engine.service.graph.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph/provider")
public class ProviderGraphController {

    @Autowired
    private GraphService graphService;

    @GetMapping("/applications")
    public ResponseEntity<GraphData<ApplicationEdge>> getProviderApplicationGraph(
            @RequestParam(required = true) String providerOrganizationName,
            @RequestParam(required = true) String providerProductName,
            @RequestParam(required = true) String providerApplicationName,
            @RequestParam(required = false, defaultValue = "false") boolean isNgbaOnly) {
        
        return ResponseEntity.ok(graphService.generateProviderApplicationGraph(
            providerOrganizationName, 
            providerProductName, 
            providerApplicationName, 
            isNgbaOnly));
    }

    @GetMapping("/products")
    public ResponseEntity<GraphData<Edge>> getProviderProductGraph(
            @RequestParam(required = true) String providerOrganizationName,
            @RequestParam(required = true) String providerProductName) {
        
        return ResponseEntity.ok(graphService.generateProviderProductGraph(
            providerOrganizationName, 
            providerProductName));
    }
} 