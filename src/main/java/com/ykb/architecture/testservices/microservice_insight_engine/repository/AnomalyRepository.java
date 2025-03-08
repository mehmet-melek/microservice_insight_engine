package com.ykb.architecture.testservices.microservice_insight_engine.repository;

import com.ykb.architecture.testservices.microservice_insight_engine.model.anomaly.Anomaly;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnomalyRepository extends MongoRepository<Anomaly, String> {
    
    List<Anomaly> findByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
            String consumerOrganizationName, 
            String consumerProductName, 
            String consumerApplicationName);
    
    List<Anomaly> findByProviderOrganizationNameAndProviderProductNameAndProviderApplicationName(
            String providerOrganizationName, 
            String providerProductName, 
            String providerApplicationName);
            
    void deleteByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
            String consumerOrganizationName, 
            String consumerProductName, 
            String consumerApplicationName);
} 