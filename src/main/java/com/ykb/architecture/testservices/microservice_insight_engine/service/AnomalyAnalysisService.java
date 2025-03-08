package com.ykb.architecture.testservices.microservice_insight_engine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykb.architecture.testservices.microservice_insight_engine.model.Anomaly;
import com.ykb.architecture.testservices.microservice_insight_engine.model.ApiAnalysis;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiAnalysisRepository;
import com.ykb.architecture.testservices.microservice_insight_engine.service.anomaly.PathAndMethodAnomalyAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class AnomalyAnalysisService {

    @Autowired
    private AnomalyService anomalyService;

    @Autowired
    private ApiAnalysisRepository apiAnalysisRepository;

    @Autowired
    private PathAndMethodAnomalyAnalyzer pathAndMethodAnomalyAnalyzer;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Gelen analiz için tüm anomali tiplerini tespit et ve kaydet
     */
    public List<Anomaly> analyzeAndSaveAnomalies(ApiAnalysis consumerAnalysis) {
        List<Anomaly> allAnomalies = new ArrayList<>();

        try {

            String consumerOrganizationName = consumerAnalysis.getOrganizationName();
            String consumerProductName = consumerAnalysis.getProductName();
            String consumerApplicationName = consumerAnalysis.getApplicationName();
            // ConsumedEndpoints'leri bir kez parse et
            JsonNode consumedEndpoints = objectMapper.readTree(consumerAnalysis.getCurrentConsumedEndpoints());

            // Geçerli provider analizlerini bir kez hazırla
            Map<String, ProviderAnalysisContext> providerAnalysisMap = prepareProviderAnalysisContexts(consumerOrganizationName,consumerProductName,consumerApplicationName,consumedEndpoints);

            // Hazırlanmış veriyle tüm analizörleri çalıştır

            // 1. Path ve Method anomalileri tespit et
            List<Anomaly> pathAndMethodAnomalies = pathAndMethodAnomalyAnalyzer.analyzePathAndMethod(providerAnalysisMap);
            allAnomalies.addAll(pathAndMethodAnomalies);

            // Diğer anomali analizörleri buraya eklenecek

        } catch (IOException e) {
            // Hata durumunda log yazılabilir
            e.printStackTrace();
        }

        // Tespit edilen anomalileri kaydet
        if (!allAnomalies.isEmpty()) {
            return anomalyService.saveAnomalies(allAnomalies);
        }

        return allAnomalies;
    }

    /**
     * ConsumedEndpoints'ten geçerli provider analizlerini hazırla
     */
    private Map<String, ProviderAnalysisContext> prepareProviderAnalysisContexts(String consumerOrganizationName,String consumerProductName,String consumerApplicationName, JsonNode consumedEndpoints) throws IOException {
        Map<String, ProviderAnalysisContext> providerMap = new HashMap<>();

        if (consumedEndpoints != null && consumedEndpoints.isArray()) {
            for (JsonNode consumedEndpoint : consumedEndpoints) {
                String clientOrgName = consumedEndpoint.has("clientOrganizationName") ?
                        consumedEndpoint.get("clientOrganizationName").asText() : null;
                String clientProductName = consumedEndpoint.has("clientProductName") ?
                        consumedEndpoint.get("clientProductName").asText() : null;
                String clientAppName = consumedEndpoint.has("clientApplicationName") ?
                        consumedEndpoint.get("clientApplicationName").asText() : null;

                // Organization, product ve application bilgilerinin hepsi null değilse analiz et
                if (isValidClientInfo(clientOrgName, clientProductName, clientAppName)) {
                    // AnalysisService yerine doğrudan repository'yi kullan
                    Optional<ApiAnalysis> providerAnalysisOpt = apiAnalysisRepository
                        .findByOrganizationNameAndProductNameAndApplicationName(
                            clientOrgName, clientProductName, clientAppName);

                    // Provider analizi varsa context oluştur
                    if (providerAnalysisOpt.isPresent()) {
                        ApiAnalysis providerAnalysis = providerAnalysisOpt.get();

                        // Provider'ın provided endpoints'lerini parse et
                        JsonNode providedEndpoints = objectMapper.readTree(providerAnalysis.getCurrentProvidedEndpoints());

                        // API çağrılarını al
                        JsonNode apiCalls = consumedEndpoint.get("apiCalls");

                        // Context oluştur
                        String providerKey = clientOrgName + ":" + clientProductName + ":" + clientAppName;
                        ProviderAnalysisContext context = new ProviderAnalysisContext(
                                consumerOrganizationName,
                                consumerProductName,
                                consumerApplicationName,
                                clientOrgName,
                                clientProductName,
                                clientAppName,
                                providedEndpoints,
                                apiCalls);

                        providerMap.put(providerKey, context);
                    }
                }
            }
        }

        return providerMap;
    }

    /**
     * Client bilgilerinin geçerli olup olmadığını kontrol et
     */
    private boolean isValidClientInfo(String orgName, String productName, String appName) {
        return (orgName != null && !orgName.isEmpty() && !orgName.equalsIgnoreCase("null")) &&
                (productName != null && !productName.isEmpty() && !productName.equalsIgnoreCase("null")) &&
                (appName != null && !appName.isEmpty() && !appName.equalsIgnoreCase("null"));
    }

    /**
     * Provider analiz bağlamı - her provider için gerekli bilgileri içerir
     */
    public static class ProviderAnalysisContext {
        private String consumerOrganizationName;
        private String consumerProductName;
        private String consumerApplicationName;
        private String providerOrganizationName; // provider
        private String providerProductName; // provider
        private String providerApplicationName; // provider
        private JsonNode providedEndpoints;
        private JsonNode apiCalls; // consumer

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
} 