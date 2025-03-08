package com.ykb.architecture.testservices.microservice_insight_engine.service.relation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ApiRelation;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.EdgeDetail;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiRelationService {

    @Autowired
    private ApiRelationRepository apiRelationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public List<ApiRelation> createApiRelations(String organizationName, String productName, String applicationName, JsonNode analysisJson) {
        // Önce bu uygulamaya (source) ait tüm ilişkileri sil
        apiRelationRepository.deleteByConsumerOrganizationAndConsumerProductAndConsumerApplication(organizationName,productName,applicationName);
        
        List<ApiRelation> relations = new ArrayList<>();
        
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
                
                relations.add(relation);
            }
        }
        
        return apiRelationRepository.saveAll(relations);
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
}
