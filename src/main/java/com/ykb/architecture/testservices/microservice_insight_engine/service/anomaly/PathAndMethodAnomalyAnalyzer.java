package com.ykb.architecture.testservices.microservice_insight_engine.service.anomaly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ykb.architecture.testservices.microservice_insight_engine.model.Anomaly;
import com.ykb.architecture.testservices.microservice_insight_engine.service.AnomalyAnalysisService.ProviderAnalysisContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PathAndMethodAnomalyAnalyzer {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Hazırlanmış provider analizleri ile path ve method anomalilerini tespit et
     */
    public List<Anomaly> analyzePathAndMethod(
            Map<String, ProviderAnalysisContext> providerAnalysisMap) {

        List<Anomaly> anomalies = new ArrayList<>();

        // Her bir provider için analiz yap
        for (ProviderAnalysisContext context : providerAnalysisMap.values()) {
            JsonNode apiCalls = context.getApiCalls();
            JsonNode providedEndpoints = context.getProvidedEndpoints();

            // Her bir API çağrısı için kontrol et
            if (apiCalls != null && apiCalls.isArray()) {
                for (JsonNode apiCall : apiCalls) {
                    // Path ve method anomalilerini tespit et
                    anomalies.addAll(detectPathAndMethodAnomalies(
                            apiCall,
                            providedEndpoints,
                            context.getConsumerOrganizationName(),
                            context.getConsumerProductName(),
                            context.getConsumerApplicationName(),
                            context.getProviderOrganizationName(),
                            context.getProviderProductName(),
                            context.getProviderApplicationName()));
                }
            }
        }

        return anomalies;
    }

    /**
     * Tek bir API çağrısı için path ve method anomalilerini tespit et
     */
    private List<Anomaly> detectPathAndMethodAnomalies(
            JsonNode apiCall,
            JsonNode providedEndpoints,
            String consumerOrganizationName,
            String consumerProductName,
            String consumerApplicationName,
            String providerOrgName,
            String providerProductName,
            String providerAppName) {

        List<Anomaly> anomalies = new ArrayList<>();

        // API çağrısının path ve method bilgilerini al
        String calledPath = apiCall.has("path") ? apiCall.get("path").asText() : "/";
        String calledMethod = apiCall.has("httpMethod") ? apiCall.get("httpMethod").asText() : "/";

        // Path değişkenleri al
        JsonNode consumedPathVariables = apiCall.has("pathVariables") ? apiCall.get("pathVariables") : null;


        boolean pathFound = false;
        boolean methodFound = false;
        // Her bir provided endpoint için kontrol et
        for (JsonNode providedEndpoint : providedEndpoints) {
            String providedPath = providedEndpoint.has("path") ? providedEndpoint.get("path").asText() : "/";
            JsonNode providedPathVariables = providedEndpoint.has("pathVariables") ? providedEndpoint.get("pathVariables") : null;

            // Path eşleşiyor mu?
            if (pathsMatch(calledPath, providedPath, consumedPathVariables, providedPathVariables)) {
                pathFound = true;
                String providedMethod = providedEndpoint.has("httpMethod") ?
                        providedEndpoint.get("httpMethod").asText() : "unknown";

                // Method uyumlu mu?
                if (calledMethod.equals(providedMethod)) {
                    methodFound = true;
                    break;
                }
            }
        }

        // Path bulunamadı - undefined endpoint anomalisi
        if (!pathFound) {
            Anomaly pathAnomaly = createUndefinedEndpointAnomaly(
                    consumerOrganizationName,
                    consumerProductName,
                    consumerApplicationName,
                    providerOrgName,
                    providerProductName,
                    providerAppName,
                    calledPath,
                    calledMethod);
            anomalies.add(pathAnomaly);
        } else if (!methodFound) {
            // Method uyumsuzluğu anomalisi
            Anomaly methodAnomaly = createUnsupportedMethodAnomaly(
                    consumerOrganizationName,
                    consumerProductName,
                    consumerApplicationName,
                    providerOrgName,
                    providerProductName,
                    providerAppName,
                    calledPath,
                    calledMethod);
            anomalies.add(methodAnomaly);
        }

        return anomalies;
    }

    /**
     * İki path'in eşleşip eşleşmediğini kontrol et
     * Path değişkenlerini dikkate alır
     */
    private boolean pathsMatch(String calledPath, String providedPath, JsonNode consumedPathVariables, JsonNode providedPathVariables) {

        // first control
        if (calledPath.equals(providedPath)) {
            return true;
        }

        // path size control
        List<String> calledSegments = splitPath(calledPath);
        List<String> providedSegments = splitPath(providedPath);

        if (calledSegments.size() != providedSegments.size()) {
            return false;
        }

        // 3. Segmentleri karşılaştır
        for (int i = 0; i < calledSegments.size(); i++) {
            String calledSeg = calledSegments.get(i);
            String providedSeg = providedSegments.get(i);

            boolean isCalledVar = isPathVariable(calledSeg);
            boolean isProvidedVar = isPathVariable(providedSeg);

            // Durum 1: Biri değişken, diğeri static → false
            if (isCalledVar != isProvidedVar) {
                return false;
            }

            // Durum 2: static → aynı olmalı
            if (!isCalledVar && !calledSeg.equals(providedSeg)) {
                return false;
            }

            // Durum 3: İkisi de değişken ise → typelarina bak type ayni ise ok, degil ise false
            if (isCalledVar) {
                String calledVarName = extractVariableName(calledSeg);
                String providedVarName = extractVariableName(providedSeg);

                String calledType = getTypeFromJson(consumedPathVariables, calledVarName);
                String providedType = getTypeFromJson(providedPathVariables, providedVarName);

                // Tip uyumsuzluğu → false
                if (!calledType.equals(providedType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<String> splitPath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(seg -> !seg.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean isPathVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }

    private String extractVariableName(String segment) {
        return segment.substring(1, segment.length() - 1);
    }

    private String getTypeFromJson(JsonNode jsonVariables, String variableName) {
        JsonNode typeNode = jsonVariables.get(variableName);
        return (typeNode != null) ? typeNode.asText() : "UNDEFINED"; // Hata yönetimi eklenebilir
    }

    /**
     * Path'i regex pattern'ına dönüştür
     * Örn: /products/{id} -> /products/([^/]+)
     */
    private String convertPathToRegex(String path) {
        return path.replaceAll("\\{[^/]+\\}", "([^/]+)");
    }

    /**
     * Undefined Endpoint anomalisi oluştur
     */
    private Anomaly createUndefinedEndpointAnomaly(
            String consumerOrganizationName,
            String consumerProductName,
            String consumerApplicationName,
            String providerOrgName,
            String providerProductName,
            String providerAppName,
            String calledPath,
            String calledMethod) {

        Anomaly anomaly = new Anomaly();

        // Consumer bilgileri
        anomaly.setConsumerOrganizationName(consumerOrganizationName);
        anomaly.setConsumerProductName(consumerProductName);
        anomaly.setConsumerApplicationName(consumerApplicationName);

        // Provider bilgileri
        anomaly.setProviderOrganizationName(providerOrgName);
        anomaly.setProviderProductName(providerProductName);
        anomaly.setProviderApplicationName(providerAppName);

        // Anomali detayları
        anomaly.setTitle("Undefined Endpoint Called");
        anomaly.setImportance("Critical");
        anomaly.setIssueType("Potential Bug");
        anomaly.setEndpoint(calledPath);
        anomaly.setHttpMethod(calledMethod);

        // Metadata
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("calledEndpoint", calledPath);
        anomaly.setMetadata(metadata);

        return anomaly;
    }

    /**
     * Unsupported HTTP Method anomalisi oluştur
     */
    private Anomaly createUnsupportedMethodAnomaly(
            String consumerOrganizationName,
            String consumerProductName,
            String consumerApplicationName,
            String providerOrgName,
            String providerProductName,
            String providerAppName,
            String calledPath,
            String calledMethod) {

        Anomaly anomaly = new Anomaly();

        // Consumer bilgileri
        anomaly.setConsumerOrganizationName(consumerOrganizationName);
        anomaly.setConsumerProductName(consumerProductName);
        anomaly.setConsumerApplicationName(consumerApplicationName);

        // Provider bilgileri
        anomaly.setProviderOrganizationName(providerOrgName);
        anomaly.setProviderProductName(providerProductName);
        anomaly.setProviderApplicationName(providerAppName);

        // Anomali detayları
        anomaly.setTitle("Unsupported HTTP Method");
        anomaly.setImportance("Critical");
        anomaly.setIssueType("Potential Bug");
        anomaly.setEndpoint(calledPath);
        anomaly.setHttpMethod(calledMethod);

        // Metadata
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("actualMethod", calledMethod);
        anomaly.setMetadata(metadata);

        return anomaly;
    }
}
