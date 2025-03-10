package com.ykb.architecture.testservices.microservice_insight_engine.dto;

import java.util.List;

public class ApplicationRelationsDTO {
    private List<ServiceDTO> providers;
    private List<ServiceDTO> consumers;
    
    public ApplicationRelationsDTO() {
    }
    
    public ApplicationRelationsDTO(List<ServiceDTO> providers, List<ServiceDTO> consumers) {
        this.providers = providers;
        this.consumers = consumers;
    }
    
    public List<ServiceDTO> getProviders() {
        return providers;
    }
    
    public void setProviders(List<ServiceDTO> providers) {
        this.providers = providers;
    }
    
    public List<ServiceDTO> getConsumers() {
        return consumers;
    }
    
    public void setConsumers(List<ServiceDTO> consumers) {
        this.consumers = consumers;
    }
} 