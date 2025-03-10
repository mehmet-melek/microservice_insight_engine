package com.ykb.architecture.testservices.microservice_insight_engine.controller;

import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ServiceEngagementChange;
import com.ykb.architecture.testservices.microservice_insight_engine.service.relation.ApiRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/engagement-changes")
public class ServiceEngagementController {

    @Autowired
    private ApiRelationService apiRelationService;

    @GetMapping
    public ResponseEntity<List<ServiceEngagementChange>> getAllEngagementChanges() {
        return ResponseEntity.ok(apiRelationService.getAllEngagementChanges());
    }

    @GetMapping("/consumer")
    public ResponseEntity<List<ServiceEngagementChange>> getEngagementChangesByConsumer(
            @RequestParam String consumerOrganizationName,
            @RequestParam String consumerProductName,
            @RequestParam String consumerApplicationName) {
        
        return ResponseEntity.ok(apiRelationService.getEngagementChangesByConsumer(
            consumerOrganizationName, consumerProductName, consumerApplicationName));
    }

    @GetMapping("/provider")
    public ResponseEntity<List<ServiceEngagementChange>> getEngagementChangesByProvider(
            @RequestParam String providerOrganizationName,
            @RequestParam String providerProductName,
            @RequestParam String providerApplicationName) {
        
        return ResponseEntity.ok(apiRelationService.getEngagementChangesByProvider(
            providerOrganizationName, providerProductName, providerApplicationName));
    }
    
    @GetMapping("/application")
    public ResponseEntity<List<ServiceEngagementChange>> getEngagementChangesByApplication(
            @RequestParam String organizationName,
            @RequestParam String productName,
            @RequestParam String applicationName) {
        
        // Hem consumer hem de provider olarak değişiklikleri al
        List<ServiceEngagementChange> consumerChanges = apiRelationService.getEngagementChangesByConsumer(
            organizationName, productName, applicationName);
            
        List<ServiceEngagementChange> providerChanges = apiRelationService.getEngagementChangesByProvider(
            organizationName, productName, applicationName);
            
        // Birleştir (ID bazında tekrarları önle)
        Set<String> addedIds = new HashSet<>();
        List<ServiceEngagementChange> combinedChanges = new ArrayList<>();
        
        // Önce consumer değişikliklerini ekle
        for (ServiceEngagementChange change : consumerChanges) {
            addedIds.add(change.getId());
            combinedChanges.add(change);
        }
        
        // Sonra provider değişikliklerini ekle (aynı ID'ye sahip olanları atlayarak)
        for (ServiceEngagementChange change : providerChanges) {
            if (!addedIds.contains(change.getId())) {
                combinedChanges.add(change);
            }
        }
        
        return ResponseEntity.ok(combinedChanges);
    }
} 