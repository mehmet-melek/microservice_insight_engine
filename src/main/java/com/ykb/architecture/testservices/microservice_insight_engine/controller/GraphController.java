package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.GraphData;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.ApplicationEdge;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.Edge;
import com.ykb.architecture.testservices.microservice_insight_engine.service.graph.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
public class GraphController {

    @Autowired
    private GraphService graphService;

    @GetMapping("/applications")
    public ResponseEntity<GraphData<ApplicationEdge>> getApplicationGraph(
            @RequestParam(required = false, defaultValue = "false") boolean isNgbaOnly) {
        return ResponseEntity.ok(graphService.generateApplicationGraph(isNgbaOnly));
    }

    @GetMapping("/products")
    public ResponseEntity<GraphData<Edge>> getProductGraph() {
        return ResponseEntity.ok(graphService.generateProductGraph());
    }
} 