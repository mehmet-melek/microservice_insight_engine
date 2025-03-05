package com.ykb.architecture.testservices.microservice_insight_engine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ykb.architecture.testservices.microservice_insight_engine.dto.ApiCallResponse;
import com.ykb.architecture.testservices.microservice_insight_engine.model.ApiAnalysis;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApiCallService {

    @Autowired
    private ApiAnalysisRepository apiAnalysisRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ApiCallResponse getApiCalls(String consumer, String provider) {
        // Consumer bilgilerini parse et
        String[] consumerParts = consumer.split("\\.");
        if (consumerParts.length != 3) {
            throw new IllegalArgumentException("Invalid consumer format. Expected: ORGANIZATION.PRODUCT.APPLICATION");
        }

        // Provider bilgilerini parse et
        String providerOrg = null;
        String providerProduct = null;
        String providerApp = null;

        if (!provider.startsWith("http")) {
            String[] providerParts = provider.split("\\.");
            if (providerParts.length == 3) {
                providerOrg = providerParts[0];
                providerProduct = providerParts[1];
                providerApp = providerParts[2];
            } else {
                providerApp = provider;
            }
        } else {
            providerApp = provider;
        }

        // Analysis'i bul
        Optional<ApiAnalysis> analysisOpt = apiAnalysisRepository
            .findByOrganizationNameAndProductNameAndApplicationName(
                consumerParts[0], consumerParts[1], consumerParts[2]);

        if (!analysisOpt.isPresent()) {
            throw new RuntimeException("Analysis not found for the given consumer");
        }

        ApiAnalysis analysis = analysisOpt.get();
        
        // String'i JsonNode'a çevir
        JsonNode consumedEndpoints = null;
        String consumedEndpointsStr = analysis.getCurrentConsumedEndpoints();
        
        if (consumedEndpointsStr != null && !consumedEndpointsStr.isEmpty()) {
            try {
                consumedEndpoints = objectMapper.readTree(consumedEndpointsStr);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing consumed endpoints", e);
            }
        }

        // Provider'a ait API çağrılarını bul
        List<JsonNode> matchingApiCalls = new ArrayList<>();
        if (consumedEndpoints != null && consumedEndpoints.isArray()) {
            for (JsonNode endpoint : consumedEndpoints) {
                boolean matches = true;
                
                if (providerOrg != null && providerProduct != null) {
                    matches = endpoint.get("clientOrganizationName").asText().equals(providerOrg) &&
                             endpoint.get("clientProductName").asText().equals(providerProduct) &&
                             endpoint.get("clientApplicationName").asText().equals(providerApp);
                } else {
                    matches = endpoint.get("clientApplicationName").asText().equals(providerApp);
                }

                if (matches && endpoint.has("apiCalls")) {
                    matchingApiCalls.add(endpoint.get("apiCalls"));
                }
            }
        }

        // Response oluştur
        ApiCallResponse response = new ApiCallResponse();
        response.setConsumer(consumer);
        response.setProvider(provider);
        response.setApiCalls(matchingApiCalls);

        return response;
    }
} 