package com.ykb.architecture.testservices.microservice_insight_engine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ykb.architecture.testservices.microservice_insight_engine.dto.AnomalyResponseDTO;
import com.ykb.architecture.testservices.microservice_insight_engine.model.Anomaly;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.AnomalyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnomalyService {

    @Autowired
    private AnomalyRepository anomalyRepository;

    /**
     * Anomalileri kaydet. Önce mevcut anomalileri sil, sonra yenilerini ekle.
     */
    @Transactional
    public List<Anomaly> saveAnomalies(List<Anomaly> anomalies) {
        if (anomalies == null || anomalies.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Aynı consumer için önceki anomalileri sil
        String consumerOrg = anomalies.get(0).getConsumerOrganizationName();
        String consumerProduct = anomalies.get(0).getConsumerProductName();
        String consumerApp = anomalies.get(0).getConsumerApplicationName();
        
        anomalyRepository.deleteByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
            consumerOrg, consumerProduct, consumerApp);
        
        // Yeni anomalileri kaydet
        return anomalyRepository.saveAll(anomalies);
    }
    
    /**
     * Consumer bazında anomalileri getir
     */
    public List<AnomalyResponseDTO> getAnomaliesByConsumer(
            String consumerOrganizationName, String consumerProductName, String consumerApplicationName) {
        
        List<Anomaly> anomalies = anomalyRepository
            .findByConsumerOrganizationNameAndConsumerProductNameAndConsumerApplicationName(
                consumerOrganizationName, consumerProductName, consumerApplicationName);
                
        return convertToResponseDTOs(anomalies);
    }
    
    /**
     * Provider bazında anomalileri getir
     */
    public List<AnomalyResponseDTO> getAnomaliesByProvider(
            String providerOrganizationName, String providerProductName, String providerApplicationName) {
        
        List<Anomaly> anomalies = anomalyRepository
            .findByProviderOrganizationNameAndProviderProductNameAndProviderApplicationName(
                providerOrganizationName, providerProductName, providerApplicationName);
                
        return convertToResponseDTOs(anomalies);
    }
    
    /**
     * Tüm anomalileri getir
     */
    public List<AnomalyResponseDTO> getAllAnomalies() {
        List<Anomaly> anomalies = anomalyRepository.findAll();
        return convertToResponseDTOs(anomalies);
    }
    
    /**
     * Anomaly'leri AnomalyResponseDTO'lara dönüştür
     * Description ve recommendation alanlarını oluştur
     */
    private List<AnomalyResponseDTO> convertToResponseDTOs(List<Anomaly> anomalies) {
        return anomalies.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Bir Anomaly'yi AnomalyResponseDTO'ya dönüştür
     * Description ve recommendation alanlarını metadata'ya göre oluştur
     */
    private AnomalyResponseDTO convertToResponseDTO(Anomaly anomaly) {
        AnomalyResponseDTO dto = new AnomalyResponseDTO();
        
        // Temel alanları kopyala
        dto.setId(anomaly.getId());
        dto.setConsumerOrganizationName(anomaly.getConsumerOrganizationName());
        dto.setConsumerProductName(anomaly.getConsumerProductName());
        dto.setConsumerApplicationName(anomaly.getConsumerApplicationName());
        dto.setProviderOrganizationName(anomaly.getProviderOrganizationName());
        dto.setProviderProductName(anomaly.getProviderProductName());
        dto.setProviderApplicationName(anomaly.getProviderApplicationName());
        dto.setTitle(anomaly.getTitle());
        dto.setImportance(anomaly.getImportance());
        dto.setIssueType(anomaly.getIssueType());
        dto.setEndpoint(anomaly.getEndpoint());
        dto.setHttpMethod(anomaly.getHttpMethod());
        dto.setMetadata(anomaly.getMetadata());
        
        // Description ve recommendation oluştur
        return generateDescriptionAndRecommendation(dto);
    }
    
    /**
     * Anomali türüne ve metadata'ya göre description ve recommendation oluştur
     */
    private AnomalyResponseDTO generateDescriptionAndRecommendation(AnomalyResponseDTO dto) {
        String title = dto.getTitle();
        JsonNode metadata = dto.getMetadata();
        
        if (title == null || metadata == null) {
            dto.setDescription("Unknown anomaly detected.");
            dto.setRecommendation("Please check the API documentation.");
            return dto;
        }
        
        // Anomali türüne göre description ve recommendation oluştur
        switch (title) {
            case "Missing Required Field in Request Body":
                String fieldName = metadata.has("fieldName") ? metadata.get("fieldName").asText() : "unknown";
                dto.setDescription(String.format("A required field '%s' is missing in the request body.", fieldName));
                dto.setRecommendation(String.format("Include the required field '%s' in the request body.", fieldName));
                break;
                
            case "Extra Field in Request Body":
                fieldName = metadata.has("fieldName") ? metadata.get("fieldName").asText() : "unknown";
                dto.setDescription(String.format("An extra field '%s' is present in the request body which is not defined in the API contract.", fieldName));
                dto.setRecommendation(String.format("Remove the extra field '%s' if it is not needed.", fieldName));
                break;
                
            case "Missing Required Request Parameter":
                String paramName = metadata.has("parameterName") ? metadata.get("parameterName").asText() : "unknown";
                dto.setDescription(String.format("The required request parameter '%s' is missing.", paramName));
                dto.setRecommendation(String.format("Provide the required request parameter '%s'.", paramName));
                break;
                
            case "Unexpected Request Parameter":
                paramName = metadata.has("parameterName") ? metadata.get("parameterName").asText() : "unknown";
                dto.setDescription(String.format("An unexpected request parameter '%s' is provided.", paramName));
                dto.setRecommendation(String.format("Remove the unexpected parameter '%s' if not required.", paramName));
                break;
                
            case "Mismatched Data Type in Request Body":
                fieldName = metadata.has("fieldName") ? metadata.get("fieldName").asText() : "unknown";
                String expectedType = metadata.has("expectedType") ? metadata.get("expectedType").asText() : "unknown";
                String actualType = metadata.has("actualType") ? metadata.get("actualType").asText() : "unknown";
                dto.setDescription(String.format("The field '%s' is sent as a %s, but a %s is expected in the request body.", fieldName, actualType, expectedType));
                dto.setRecommendation(String.format("Send the field '%s' as a %s.", fieldName, expectedType));
                break;
                
            case "Mismatched Data Type in Request Parameter":
                paramName = metadata.has("parameterName") ? metadata.get("parameterName").asText() : "unknown";
                expectedType = metadata.has("expectedType") ? metadata.get("expectedType").asText() : "unknown";
                actualType = metadata.has("actualType") ? metadata.get("actualType").asText() : "unknown";
                dto.setDescription(String.format("The request parameter '%s' is sent as a %s, but a %s is expected.", paramName, actualType, expectedType));
                dto.setRecommendation(String.format("Adjust the data type of '%s' to a %s.", paramName, expectedType));
                break;
                
            case "Mismatched Path Variable Data Type":
                String pathVarName = metadata.has("pathVariableName") ? metadata.get("pathVariableName").asText() : "unknown";
                expectedType = metadata.has("expectedType") ? metadata.get("expectedType").asText() : "unknown";
                actualType = metadata.has("actualType") ? metadata.get("actualType").asText() : "unknown";
                dto.setDescription(String.format("The path variable '%s' is provided as a %s, but a %s is expected.", pathVarName, actualType, expectedType));
                dto.setRecommendation(String.format("Ensure that the '%s' in the URL is of type %s.", pathVarName, expectedType));
                break;
                
            case "Undefined Endpoint Called":
                String calledEndpoint = metadata.has("calledEndpoint") ? metadata.get("calledEndpoint").asText() : "unknown";
                dto.setDescription(String.format("Service A calls an endpoint '%s' which is not defined in Service B's API contract.", calledEndpoint));
                dto.setRecommendation("Update Service A to call one of the defined endpoints as per the API documentation.");
                break;
                
            case "Unsupported HTTP Method":
                String expectedMethod = metadata.has("expectedMethod") ? metadata.get("expectedMethod").asText() : "unknown";
                String actualMethod = metadata.has("actualMethod") ? metadata.get("actualMethod").asText() : "unknown";
                dto.setDescription(String.format("Service A calls the endpoint '%s' using the HTTP method %s, which is not supported. The API contract specifies that this endpoint only supports %s.", dto.getEndpoint(), actualMethod, expectedMethod));
                dto.setRecommendation(String.format("Update Service A to use the correct HTTP method (%s) for the '%s' endpoint.", expectedMethod, dto.getEndpoint()));
                break;
                
            default:
                dto.setDescription("Unknown anomaly detected: " + title);
                dto.setRecommendation("Please check the API documentation.");
                break;
        }
        return dto;
    }

}