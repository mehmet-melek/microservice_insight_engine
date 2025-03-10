package com.ykb.architecture.testservices.microservice_insight_engine.dto;

import java.util.List;

public class ServiceDTO {
    private String organizationName;
    private String productName;
    private String applicationName;
    private List<PathDTO> paths;
    
    public ServiceDTO() {
    }
    
    public ServiceDTO(String organizationName, String productName, String applicationName, List<PathDTO> paths) {
        this.organizationName = organizationName;
        this.productName = productName;
        this.applicationName = applicationName;
        this.paths = paths;
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
    
    public List<PathDTO> getPaths() {
        return paths;
    }
    
    public void setPaths(List<PathDTO> paths) {
        this.paths = paths;
    }
} 