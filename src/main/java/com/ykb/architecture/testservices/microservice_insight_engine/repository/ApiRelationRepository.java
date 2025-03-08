package com.ykb.architecture.testservices.microservice_insight_engine.repository;

import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ApiRelation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApiRelationRepository extends MongoRepository<ApiRelation, String> {
    void deleteByConsumerOrganizationAndConsumerProductAndConsumerApplication(String organization, String product, String application);
    
    List<ApiRelation> findByConsumerOrganizationAndConsumerProductAndConsumerApplication(
            String organization, String product, String application);
            
    List<ApiRelation> findByProviderOrganizationAndProviderProductAndProviderApplication(
            String organization, String product, String application);

    List<ApiRelation> findByConsumerOrganizationAndConsumerProduct(
            String consumerOrganization, String consumerProduct);

    List<ApiRelation> findByProviderOrganizationAndProviderProduct(
            String providerOrganization, String providerProduct);
}