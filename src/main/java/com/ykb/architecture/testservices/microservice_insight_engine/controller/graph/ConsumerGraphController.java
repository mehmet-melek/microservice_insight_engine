package com.ykb.architecture.testservices.microservice_insight_engine.controller.graph;

import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.ApplicationEdge;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.Edge;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.GraphData;
import com.ykb.architecture.testservices.microservice_insight_engine.service.graph.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph/consumer")
public class ConsumerGraphController {

    @Autowired
    private GraphService graphService;


    @GetMapping("/applications")
    public ResponseEntity<GraphData<ApplicationEdge>> getConsumerApplicationGraph(
            @RequestParam(required = true) String consumerOrganizationName,
            @RequestParam(required = true) String consumerProductName,
            @RequestParam(required = true) String consumerApplicationName,
            @RequestParam(required = false, defaultValue = "false") boolean isNgbaOnly) {
        
        return ResponseEntity.ok(graphService.generateConsumerApplicationGraph(
            consumerOrganizationName, 
            consumerProductName, 
            consumerApplicationName, 
            isNgbaOnly));
    }

    @GetMapping("/products")
    public ResponseEntity<GraphData<Edge>> getConsumerProductGraph(
            @RequestParam(required = true) String consumerOrganizationName,
            @RequestParam(required = true) String consumerProductName) {
        
        return ResponseEntity.ok(graphService.generateConsumerProductGraph(
            consumerOrganizationName, 
            consumerProductName));
    }

} 