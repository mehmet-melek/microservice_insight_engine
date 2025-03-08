package com.ykb.architecture.testservices.microservice_insight_engine.service;

import com.ykb.architecture.testservices.microservice_insight_engine.model.ApiAnalysis;
import com.ykb.architecture.testservices.microservice_insight_engine.model.ApplicationInfo;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiAnalysisRepository;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApplicationInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnalysisService {

    @Autowired
    private ApiAnalysisRepository analysisRepository;

    @Autowired
    private ApplicationInfoRepository applicationInfoRepository;

    @Autowired
    private AnomalyAnalysisService anomalyAnalysisService;

    @Transactional
    public ApiAnalysis saveOrUpdateAnalysis(ApiAnalysis newAnalysis) {
        // Önce ApplicationInfo'yu kaydet/güncelle
        saveOrUpdateApplicationInfo(
            newAnalysis.getOrganizationName(),
            newAnalysis.getProductName(),
            newAnalysis.getApplicationName()
        );

        Optional<ApiAnalysis> existingOpt = analysisRepository.findByOrganizationNameAndProductNameAndApplicationName(
                newAnalysis.getOrganizationName(),
                newAnalysis.getProductName(),
                newAnalysis.getApplicationName());

        ApiAnalysis savedAnalysis;
        if(existingOpt.isPresent()){
            ApiAnalysis existing = existingOpt.get();
            existing.setPreviousConsumedEndpoints(existing.getCurrentConsumedEndpoints());
            existing.setPreviousProvidedEndpoints(existing.getCurrentProvidedEndpoints());
            existing.setCurrentConsumedEndpoints(newAnalysis.getCurrentConsumedEndpoints());
            existing.setCurrentProvidedEndpoints(newAnalysis.getCurrentProvidedEndpoints());
            existing.setUpdatedAt(LocalDateTime.now());

            savedAnalysis = analysisRepository.save(existing);
        } else {
            newAnalysis.setUpdatedAt(LocalDateTime.now());
            savedAnalysis = analysisRepository.save(newAnalysis);
        }
        
        //TODO: Anomali analizini asenkron olarak başlat
        if (savedAnalysis.getCurrentConsumedEndpoints() != null && !savedAnalysis.getCurrentConsumedEndpoints().isEmpty()) {
            analyzeAnomaliesAsync(savedAnalysis);
        }
        
        return savedAnalysis;
    }

    private void saveOrUpdateApplicationInfo(String organizationName, String productName, String applicationName) {
        // Null veya empty string kontrolü
        String orgName = (organizationName == null || organizationName.isEmpty() || 
                         organizationName.equalsIgnoreCase("null")) ? null : organizationName;
        String prodName = (productName == null || productName.isEmpty() || 
                          productName.equalsIgnoreCase("null")) ? null : productName;

        Optional<ApplicationInfo> existingOpt;
        
        if (orgName == null && prodName == null) {
            // Eğer organization ve product null ise sadece application name'e göre kontrol et
            existingOpt = applicationInfoRepository
                .findByApplicationNameAndOrganizationNameIsNullAndProductNameIsNull(applicationName);
        } else {
            // Tüm alanları kullanarak kontrol et
            existingOpt = applicationInfoRepository
                .findByOrganizationNameAndProductNameAndApplicationName(orgName, prodName, applicationName);
        }

        if (!existingOpt.isPresent()) {
            // Kayıt yoksa yeni kayıt oluştur
            ApplicationInfo appInfo = new ApplicationInfo(orgName, prodName, applicationName);
            applicationInfoRepository.save(appInfo);
        }
        // Kayıt varsa hiçbir şey yapma
    }

    public Optional<ApiAnalysis> findAnalysis(String organizationName, String productName, String applicationName) {
        return analysisRepository.findByOrganizationNameAndProductNameAndApplicationName(
                organizationName, productName, applicationName);
    }

    /**
     * Anomali analizini asenkron olarak çalıştır
     * Bu sayede API yanıt süresi uzamayacak
     */
    private void analyzeAnomaliesAsync(ApiAnalysis analysis) {
        // Asenkron çağrı (örn. @Async veya CompletableFuture kullanılabilir)
        new Thread(() -> {
            try {
                anomalyAnalysisService.analyzeAndSaveAnomalies(analysis);
            } catch (Exception e) {
                // Hata durumunda loglama yapılabilir
                e.printStackTrace();
            }
        }).start();
    }

    // Diff hesaplama ve diğer iş mantığı metotlarını ekleyin.
}