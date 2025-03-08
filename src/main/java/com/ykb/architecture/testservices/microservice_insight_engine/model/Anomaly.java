package com.ykb.architecture.testservices.microservice_insight_engine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.JsonNode;

@Document(collection = "anomalies")
public class Anomaly {
    
    @Id
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
    
    // Metadata JSON olarak saklanacak (farklı anomali türleri için farklı yapılar)
    private JsonNode metadata;

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

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }
} 