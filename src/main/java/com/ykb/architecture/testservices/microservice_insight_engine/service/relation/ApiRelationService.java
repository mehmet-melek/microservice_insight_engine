package com.ykb.architecture.testservices.microservice_insight_engine.service.relation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ApiRelation;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.EdgeDetail;
import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ServiceEngagementChange;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiRelationRepository;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ServiceEngagementChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiRelationService {

    @Autowired
    private ApiRelationRepository apiRelationRepository;
    
    @Autowired
    private ServiceEngagementChangeRepository serviceEngagementChangeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public List<ApiRelation> createApiRelations(String organizationName, String productName, String applicationName, JsonNode analysisJson) {
        // Önce mevcut ilişkileri al (silmeden önce)
        List<ApiRelation> existingRelations = apiRelationRepository
                .findByConsumerOrganizationAndConsumerProductAndConsumerApplication(organizationName, productName, applicationName);
        
        // Sonra yeni ilişkileri oluştur
        List<ApiRelation> newRelations = new ArrayList<>();
        
        JsonNode consumedEndpoints = analysisJson.get("consumedEndpoints");
        if (consumedEndpoints != null && consumedEndpoints.isArray()) {
            for (JsonNode consumed : consumedEndpoints) {
                String clientOrg = consumed.get("clientOrganizationName").asText();
                String clientProduct = consumed.get("clientProductName").asText();
                String clientApp = consumed.get("clientApplicationName").asText();

                // Her consumed endpoint için tek bir relation oluştur
                ApiRelation relation = new ApiRelation();
                relation.setConsumerOrganization(organizationName);
                relation.setConsumerProduct(productName);
                relation.setConsumerApplication(applicationName);
                relation.setProviderOrganization(clientOrg);
                relation.setProviderProduct(clientProduct);
                relation.setProviderApplication(clientApp);
                
                // API çağrılarını details listesine ekle
                List<EdgeDetail> details = new ArrayList<>();
                JsonNode apiCalls = consumed.get("apiCalls");
                if (apiCalls != null && apiCalls.isArray()) {
                    for (JsonNode call : apiCalls) {
                        EdgeDetail detail = new EdgeDetail();
                        detail.setMethod(call.get("httpMethod").asText());
                        detail.setPath(call.get("path").asText());
                        details.add(detail);
                    }
                }
                relation.setDetails(details);
                
                newRelations.add(relation);
            }
        }
        
        // Değişiklikleri tespit et ve kaydet
        if (!existingRelations.isEmpty()) {
            detectAndSaveEngagementChanges(existingRelations, newRelations, organizationName, productName, applicationName);
        }
        
        // Eski ilişkileri sil
        apiRelationRepository.deleteByConsumerOrganizationAndConsumerProductAndConsumerApplication(
                organizationName, productName, applicationName);
        
        // Yeni ilişkileri kaydet
        return apiRelationRepository.saveAll(newRelations);
    }
    
    /**
     * Eski ve yeni ilişkiler arasındaki değişiklikleri tespit et ve kaydet
     */
    private void detectAndSaveEngagementChanges(
            List<ApiRelation> existingRelations, 
            List<ApiRelation> newRelations,
            String consumerOrg,
            String consumerProduct,
            String consumerApp) {
        
        // Kolay erişim için mevcut ilişkileri map'e ekle
        Map<String, ApiRelation> existingRelationsMap = new HashMap<>();
        for (ApiRelation relation : existingRelations) {
            String key = getRelationKey(relation);
            existingRelationsMap.put(key, relation);
        }
        
        // Kolay erişim için yeni ilişkileri map'e ekle
        Map<String, ApiRelation> newRelationsMap = new HashMap<>();
        for (ApiRelation relation : newRelations) {
            String key = getRelationKey(relation);
            newRelationsMap.put(key, relation);
        }
        
        List<ServiceEngagementChange> changes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Başlayan engagementları tespit et (yeni ilişkilerde var, eskide yok)
        for (ApiRelation newRelation : newRelations) {
            String key = getRelationKey(newRelation);
            if (!existingRelationsMap.containsKey(key)) {
                // Yeni başlayan engagement
                ServiceEngagementChange change = new ServiceEngagementChange(
                    consumerOrg,
                    consumerProduct,
                    consumerApp,
                    newRelation.getProviderOrganization(),
                    newRelation.getProviderProduct(),
                    newRelation.getProviderApplication(),
                    ServiceEngagementChange.ChangeType.EngagementStarted,
                    now,
                    String.format("%s has started consuming service %s.",
                        consumerApp, newRelation.getProviderApplication())
                );
                changes.add(change);
            }
        }
        
        // Biten engagementları tespit et (eski ilişkilerde var, yenide yok)
        for (ApiRelation existingRelation : existingRelations) {
            String key = getRelationKey(existingRelation);
            if (!newRelationsMap.containsKey(key)) {
                // Sona eren engagement
                ServiceEngagementChange change = new ServiceEngagementChange(
                    consumerOrg,
                    consumerProduct,
                    consumerApp,
                    existingRelation.getProviderOrganization(),
                    existingRelation.getProviderProduct(),
                    existingRelation.getProviderApplication(),
                    ServiceEngagementChange.ChangeType.EngagementEnded,
                    now,
                    String.format("%s has stopped consuming service %s.",
                        consumerApp, existingRelation.getProviderApplication())
                );
                changes.add(change);
            }
        }
        
        // Değişiklikleri kaydet
        if (!changes.isEmpty()) {
            serviceEngagementChangeRepository.saveAll(changes);
        }
    }
    
    /**
     * İlişki için benzersiz bir anahtar oluştur
     */
    private String getRelationKey(ApiRelation relation) {
        return relation.getProviderOrganization() + ":" + 
               relation.getProviderProduct() + ":" + 
               relation.getProviderApplication();
    }

    public List<ApiRelation> getAllRelations() {
        return apiRelationRepository.findAll();
    }

    public List<ApiRelation> getRelationsByConsumer(String organization, String product, String application) {
        return apiRelationRepository.findByConsumerOrganizationAndConsumerProductAndConsumerApplication(
                organization, product, application);
    }

    public List<ApiRelation> getRelationsByProvider(String organization, String product, String application) {
        return apiRelationRepository.findByProviderOrganizationAndProviderProductAndProviderApplication(
                organization, product, application);
    }
    
    public List<ServiceEngagementChange> getEngagementChangesByConsumer(
            String consumerOrg, String consumerProduct, String consumerApp) {
        return serviceEngagementChangeRepository
            .findByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
                consumerOrg, consumerProduct, consumerApp);
    }
    
    public List<ServiceEngagementChange> getEngagementChangesByProvider(
            String providerOrg, String providerProduct, String providerApp) {
        return serviceEngagementChangeRepository
            .findByProviderOrganizationNameAndProviderProductNameAndProviderApplicationName(
                providerOrg, providerProduct, providerApp);
    }
    
    public List<ServiceEngagementChange> getAllEngagementChanges() {
        return serviceEngagementChangeRepository.findAll();
    }
}
