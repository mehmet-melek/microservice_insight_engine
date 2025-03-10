package com.ykb.architecture.testservices.microservice_insight_engine.dto;

public class PathDTO {
    private String path;
    private String method;
    
    public PathDTO() {
    }
    
    public PathDTO(String path, String method) {
        this.path = path;
        this.method = method;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
} 