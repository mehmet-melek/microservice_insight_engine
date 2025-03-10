package com.ykb.architecture.testservices.microservice_insight_engine.model.relation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "service_engagement_changes")
public class ServiceEngagementChange {
    
    @Id
    private String id;
    
    private String consumerOrganizationName;
    private String consumerProductName;
    private String consumerApplicationName;
    
    private String providerOrganizationName;
    private String providerProductName;
    private String providerApplicationName;
    
    private ChangeType changeType;
    private LocalDateTime timestamp;
    private String details;
    
    public enum ChangeType {
        EngagementStarted,
        EngagementEnded
    }
    
    // Constructors, getters and setters
    public ServiceEngagementChange() {
    }
    
    public ServiceEngagementChange(String consumerOrganizationName, String consumerProductName, 
                                  String consumerApplicationName, String providerOrganizationName, 
                                  String providerProductName, String providerApplicationName, 
                                  ChangeType changeType, LocalDateTime timestamp, String details) {
        this.consumerOrganizationName = consumerOrganizationName;
        this.consumerProductName = consumerProductName;
        this.consumerApplicationName = consumerApplicationName;
        this.providerOrganizationName = providerOrganizationName;
        this.providerProductName = providerProductName;
        this.providerApplicationName = providerApplicationName;
        this.changeType = changeType;
        this.timestamp = timestamp;
        this.details = details;
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

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
} 