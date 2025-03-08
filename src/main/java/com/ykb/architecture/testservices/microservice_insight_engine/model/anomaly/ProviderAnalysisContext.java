package com.ykb.architecture.testservices.microservice_insight_engine.model.anomaly;

import com.fasterxml.jackson.databind.JsonNode;

public class ProviderAnalysisContext {
    private String consumerOrganizationName;
    private String consumerProductName;
    private String consumerApplicationName;
    private String providerOrganizationName; // provider
    private String providerProductName; // provider
    private String providerApplicationName; // provider
    private JsonNode providedEndpoints;
    private JsonNode apiCalls; // consumer


    public ProviderAnalysisContext() {
    }

    public ProviderAnalysisContext(String consumerOrganizationName, String consumerProductName, String consumerApplicationName, String providerOrganizationName, String providerProductName, String providerApplicationName, JsonNode providedEndpoints, JsonNode apiCalls) {
        this.consumerOrganizationName = consumerOrganizationName;
        this.consumerProductName = consumerProductName;
        this.consumerApplicationName = consumerApplicationName;
        this.providerOrganizationName = providerOrganizationName;
        this.providerProductName = providerProductName;
        this.providerApplicationName = providerApplicationName;
        this.providedEndpoints = providedEndpoints;
        this.apiCalls = apiCalls;
    }

    public String getConsumerOrganizationName() {
        return consumerOrganizationName;
    }

    public String getConsumerProductName() {
        return consumerProductName;
    }

    public String getConsumerApplicationName() {
        return consumerApplicationName;
    }

    public String getProviderOrganizationName() {
        return providerOrganizationName;
    }

    public String getProviderProductName() {
        return providerProductName;
    }

    public String getProviderApplicationName() {
        return providerApplicationName;
    }

    public JsonNode getProvidedEndpoints() {
        return providedEndpoints;
    }

    public JsonNode getApiCalls() {
        return apiCalls;
    }
}