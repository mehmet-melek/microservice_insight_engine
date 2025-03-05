package com.ykb.architecture.testservices.microservice_insight_engine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "application_info")
@CompoundIndex(name = "app_unique_idx", 
    def = "{'organizationName': 1, 'productName': 1, 'applicationName': 1}", 
    unique = true)
public class ApplicationInfo {
    @Id
    private String id;
    private String organizationName;
    private String productName;
    private String applicationName;

    public ApplicationInfo() {
    }

    public ApplicationInfo(String organizationName, String productName, String applicationName) {
        this.organizationName = organizationName == null || organizationName.isEmpty() ? null : organizationName;
        this.productName = productName == null || productName.isEmpty() ? null : productName;
        this.applicationName = applicationName;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}