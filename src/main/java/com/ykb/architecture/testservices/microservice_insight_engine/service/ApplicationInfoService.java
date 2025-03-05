package com.ykb.architecture.testservices.microservice_insight_engine.service;

import com.ykb.architecture.testservices.microservice_insight_engine.model.ApplicationInfo;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApplicationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationInfoService {

    @Autowired
    private ApplicationInfoRepository applicationInfoRepository;

    public List<ApplicationInfo> getAllApplications(boolean isNgbaOnly) {
        if (isNgbaOnly) {
            return applicationInfoRepository.findAll().stream()
                .filter(app -> app.getOrganizationName() != null && 
                             !app.getOrganizationName().isEmpty() &&
                             !app.getOrganizationName().equalsIgnoreCase("null") &&
                             app.getProductName() != null && 
                             !app.getProductName().isEmpty() &&
                             !app.getProductName().equalsIgnoreCase("null"))
                .collect(Collectors.toList());
        }
        return applicationInfoRepository.findAll();
    }
} 