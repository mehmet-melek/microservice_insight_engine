package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.dto.ApplicationRelationsDTO;
import com.ykb.architecture.testservices.microservice_insight_engine.service.relation.ApiRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/application-relations")
public class ApplicationRelationsController {

    @Autowired
    private ApiRelationService apiRelationService;

    @GetMapping
    public ResponseEntity<ApplicationRelationsDTO> getApplicationRelations(
            @RequestParam String organizationName,
            @RequestParam String productName,
            @RequestParam String applicationName) {
        
        ApplicationRelationsDTO relations = apiRelationService.getApplicationRelations(
            organizationName, productName, applicationName);
            
        return ResponseEntity.ok(relations);
    }
} 