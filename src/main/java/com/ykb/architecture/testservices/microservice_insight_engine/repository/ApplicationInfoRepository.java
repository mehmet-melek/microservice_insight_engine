package com.ykb.architecture.testservices.microservice_insight_engine.repository;

import com.ykb.architecture.testservices.microservice_insight_engine.model.ApplicationInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ApplicationInfoRepository extends MongoRepository<ApplicationInfo, String> {
    Optional<ApplicationInfo> findByOrganizationNameAndProductNameAndApplicationName(
            String organizationName, String productName, String applicationName);
            
    Optional<ApplicationInfo> findByApplicationNameAndOrganizationNameIsNullAndProductNameIsNull(
            String applicationName);
} 