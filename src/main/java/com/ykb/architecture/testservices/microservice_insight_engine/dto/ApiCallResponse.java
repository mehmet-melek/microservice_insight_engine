package com.ykb.architecture.testservices.microservice_insight_engine.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;


public class ApiCallResponse {
    private String consumer;
    private String provider;
    private List<JsonNode> apiCalls;


    public ApiCallResponse() {
    }

    public ApiCallResponse(String consumer, String provider, List<JsonNode> apiCalls) {
        this.consumer = consumer;
        this.provider = provider;
        this.apiCalls = apiCalls;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<JsonNode> getApiCalls() {
        return apiCalls;
    }

    public void setApiCalls(List<JsonNode> apiCalls) {
        this.apiCalls = apiCalls;
    }
}