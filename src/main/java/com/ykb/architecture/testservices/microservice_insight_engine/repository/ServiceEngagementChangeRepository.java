package com.ykb.architecture.testservices.microservice_insight_engine.repository;

import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ServiceEngagementChange;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceEngagementChangeRepository extends MongoRepository<ServiceEngagementChange, String> {
    
    List<ServiceEngagementChange> findByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
            String consumerOrganizationName, 
            String consumerProductName, 
            String consumerApplicationName);
            
    List<ServiceEngagementChange> findByProviderOrganizationNameAndProviderProductNameAndProviderApplicationName(
            String providerOrganizationName, 
            String providerProductName, 
            String providerApplicationName);
} 