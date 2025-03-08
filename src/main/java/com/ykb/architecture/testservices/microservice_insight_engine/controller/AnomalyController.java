package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.dto.AnomalyResponseDTO;
import com.ykb.architecture.testservices.microservice_insight_engine.service.anomaly.AnomalyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anomalies")
public class AnomalyController {

    @Autowired
    private AnomalyService anomalyService;


    @GetMapping
    public ResponseEntity<List<AnomalyResponseDTO>> getAllAnomalies() {
        return ResponseEntity.ok(anomalyService.getAllAnomalies());
    }

    @GetMapping("/consumer")
    public ResponseEntity<List<AnomalyResponseDTO>> getAnomaliesByConsumer(
            @RequestParam String consumerOrganizationName,
            @RequestParam String consumerProductName,
            @RequestParam String consumerApplicationName) {
        
        return ResponseEntity.ok(anomalyService.getAnomaliesByConsumer(
            consumerOrganizationName, consumerProductName, consumerApplicationName));
    }

    @GetMapping("/provider")
    public ResponseEntity<List<AnomalyResponseDTO>> getAnomaliesByProvider(
            @RequestParam String providerOrganizationName,
            @RequestParam String providerProductName,
            @RequestParam String providerApplicationName) {
        
        return ResponseEntity.ok(anomalyService.getAnomaliesByProvider(
            providerOrganizationName, providerProductName, providerApplicationName));
    }
} 