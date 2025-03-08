package com.ykb.architecture.testservices.microservice_insight_engine.dto;

import com.fasterxml.jackson.databind.JsonNode;

public class AnomalyResponseDTO {
    
    private String id;
    
    private String consumerOrganizationName;
    private String consumerProductName;
    private String consumerApplicationName;
    
    private String providerOrganizationName;
    private String providerProductName;
    private String providerApplicationName;
    
    private String title;
    private String importance;
    private String issueType;
    private String endpoint;
    private String httpMethod;
    private String description;  // Veritabanında saklanmıyor, raporlamada oluşturuluyor
    private String recommendation;  // Veritabanında saklanmıyor, raporlamada oluşturuluyor
    
    private JsonNode metadata;

    public AnomalyResponseDTO() {
    }

    public AnomalyResponseDTO(String id, String consumerOrganizationName, String consumerProductName, String consumerApplicationName, String providerOrganizationName, String providerProductName, String providerApplicationName, String title, String importance, String issueType, String endpoint, String httpMethod, String description, String recommendation, JsonNode metadata) {
        this.id = id;
        this.consumerOrganizationName = consumerOrganizationName;
        this.consumerProductName = consumerProductName;
        this.consumerApplicationName = consumerApplicationName;
        this.providerOrganizationName = providerOrganizationName;
        this.providerProductName = providerProductName;
        this.providerApplicationName = providerApplicationName;
        this.title = title;
        this.importance = importance;
        this.issueType = issueType;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.description = description;
        this.recommendation = recommendation;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConsumerOrganizationName() {
        return consumerOrganizationName;
    }

    public void setConsumerOrganizationName(String consumerOrganizationName) {
        this.consumerOrganizationName = consumerOrganizationName;
    }

    public String getConsumerProductName() {
        return consumerProductName;
    }

    public void setConsumerProductName(String consumerProductName) {
        this.consumerProductName = consumerProductName;
    }

    public String getConsumerApplicationName() {
        return consumerApplicationName;
    }

    public void setConsumerApplicationName(String consumerApplicationName) {
        this.consumerApplicationName = consumerApplicationName;
    }

    public String getProviderOrganizationName() {
        return providerOrganizationName;
    }

    public void setProviderOrganizationName(String providerOrganizationName) {
        this.providerOrganizationName = providerOrganizationName;
    }

    public String getProviderProductName() {
        return providerProductName;
    }

    public void setProviderProductName(String providerProductName) {
        this.providerProductName = providerProductName;
    }

    public String getProviderApplicationName() {
        return providerApplicationName;
    }

    public void setProviderApplicationName(String providerApplicationName) {
        this.providerApplicationName = providerApplicationName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }
} 