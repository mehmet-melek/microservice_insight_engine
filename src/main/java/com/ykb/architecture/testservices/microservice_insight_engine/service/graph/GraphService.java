package com.ykb.architecture.testservices.microservice_insight_engine.service.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykb.architecture.testservices.microservice_insight_engine.model.relation.ApiRelation;
import com.ykb.architecture.testservices.microservice_insight_engine.model.graph.*;
import com.ykb.architecture.testservices.microservice_insight_engine.repository.ApiRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {

    @Autowired
    private ApiRelationRepository apiRelationRepository;


    @Autowired
    private ObjectMapper objectMapper;

    public GraphData<ApplicationEdge> generateApplicationGraph(boolean isNgbaOnly) {
        List<ApiRelation> relations = apiRelationRepository.findAll();
        
        Set<String> applicationNames = new HashSet<>();
        Map<String, ApplicationInfo> appInfoMap = new HashMap<>();

        relations.forEach(relation -> {
            // Consumer application bilgileri
            String consumerFullName = buildFullName(
                relation.getConsumerOrganization(), 
                relation.getConsumerProduct(), 
                relation.getConsumerApplication()
            );
            
            if (!isNgbaOnly || (isNgbaOnly && isValidProduct(relation.getConsumerProduct()))) {
                applicationNames.add(consumerFullName);
                appInfoMap.put(consumerFullName, new ApplicationInfo(
                    relation.getConsumerOrganization(),
                    relation.getConsumerProduct(),
                    relation.getConsumerApplication()
                ));
            }
            
            // Provider application bilgileri
            String providerFullName = buildFullName(
                relation.getProviderOrganization(), 
                relation.getProviderProduct(), 
                relation.getProviderApplication()
            );
            
            if (!isNgbaOnly || (isNgbaOnly && isValidProduct(relation.getProviderProduct()))) {
                applicationNames.add(providerFullName);
                appInfoMap.put(providerFullName, new ApplicationInfo(
                    relation.getProviderOrganization(),
                    relation.getProviderProduct(),
                    relation.getProviderApplication()
                ));
            }
        });

        // Node'ları oluştur
        List<Node> nodes = applicationNames.stream()
                .map(fullName -> {
                    Node node = new Node();
                    ApplicationInfo appInfo = appInfoMap.get(fullName);
                    
                    // Eğer organization ve product null/empty ise direkt application name'i kullan
                    if ((appInfo.organizationName == null || appInfo.organizationName.isEmpty() || 
                         appInfo.organizationName.equalsIgnoreCase("null")) &&
                        (appInfo.productName == null || appInfo.productName.isEmpty() || 
                         appInfo.productName.equalsIgnoreCase("null"))) {
                        node.setName(appInfo.applicationName);
                        node.setLabel(appInfo.applicationName);
                    } else {
                        node.setName(fullName);
                        node.setLabel(appInfo.applicationName);
                    }
                    
                    node.setType("application");
                    return node;
                })
                .collect(Collectors.toList());

        // Edge'leri oluştur
        Map<String, ApplicationEdge> edgeMap = new HashMap<>();
        
        relations.forEach(relation -> {
            String sourceFullName = buildFullName(
                relation.getConsumerOrganization(),
                relation.getConsumerProduct(),
                relation.getConsumerApplication()
            );
            
            String targetFullName = buildFullName(
                relation.getProviderOrganization(),
                relation.getProviderProduct(),
                relation.getProviderApplication()
            );

            // isNgbaOnly=true ise ve product null ise bu ilişkiyi atla
            if (isNgbaOnly && (!isValidProduct(relation.getConsumerProduct()) || 
                             !isValidProduct(relation.getProviderProduct()))) {
                return;
            }

            String edgeKey = sourceFullName + "->" + targetFullName;
            
            ApplicationEdge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                ApplicationEdge e = new ApplicationEdge();
                e.setSource(sourceFullName);
                e.setTarget(targetFullName);
                e.setDetails(new ArrayList<>());
                return e;
            });
            
            edge.getDetails().addAll(relation.getDetails());
        });

        GraphData<ApplicationEdge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));
        
        return graphData;
    }

    // Application bilgilerini tutmak için yardımcı sınıf
    private static class ApplicationInfo {
        String organizationName;
        String productName;
        String applicationName;

        ApplicationInfo(String organizationName, String productName, String applicationName) {
            this.organizationName = organizationName;
            this.productName = productName;
            this.applicationName = applicationName;
        }
    }

    // Tam isim oluşturmak için yardımcı metod
    private String buildFullName(String organization, String product, String application) {
        // Eğer organization ve product null/empty ise direkt application name'i döndür
        if ((organization == null || organization.isEmpty() || organization.equalsIgnoreCase("null")) &&
            (product == null || product.isEmpty() || product.equalsIgnoreCase("null"))) {
            return application;
        }
        return String.format("%s.%s.%s", organization, product, application);
    }

    private boolean containsSameDetail(List<EdgeDetail> details, EdgeDetail newDetail) {
        return details.stream().anyMatch(detail ->
                detail.getMethod().equals(newDetail.getMethod()) &&
                detail.getPath().equals(newDetail.getPath())
        );
    }

    public GraphData<Edge> generateProductGraph() {
        List<ApiRelation> relations = apiRelationRepository.findAll();
        Set<String> productNames = new HashSet<>();
        Map<String, Edge> edgeMap = new HashMap<>();

        relations.forEach(relation -> {
            String sourceProduct = buildProductName(
                relation.getConsumerOrganization(), 
                relation.getConsumerProduct()
            );
            String targetProduct = buildProductName(
                relation.getProviderOrganization(), 
                relation.getProviderProduct()
            );
            
            // Source ve target aynı ise veya geçersiz product ise atla
            if (sourceProduct.equals(targetProduct) || 
                !isValidProduct(relation.getConsumerProduct()) || 
                !isValidProduct(relation.getProviderProduct())) {
                return;
            }

            productNames.add(sourceProduct);
            productNames.add(targetProduct);

            String edgeKey = sourceProduct + "->" + targetProduct;
            Edge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                Edge e = new Edge();
                e.setSource(sourceProduct);
                e.setTarget(targetProduct);
                e.setApplications(new HashSet<>());
                return e;
            });

            // Application'ı edge'e ekle (son parçayı al)
            String applicationLabel = relation.getConsumerApplication()
                .substring(relation.getConsumerApplication().lastIndexOf('.') + 1);
            edge.getApplications().add(applicationLabel);
        });

        // Node'ları oluştur
        List<Node> nodes = productNames.stream()
                .map(productName -> {
                    Node node = new Node();
                    node.setName(productName);
                    // Label için product name'in son parçasını al
                    node.setLabel(productName.substring(productName.lastIndexOf('.') + 1));
                    node.setType("product");
                    return node;
                })
                .collect(Collectors.toList());

        GraphData<Edge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));

        return graphData;
    }

    // Product name oluşturmak için yardımcı metod
    private String buildProductName(String organization, String product) {
        // Product null/empty ise organization'ı da kontrol et
        if (product == null || product.isEmpty() || product.equalsIgnoreCase("null")) {
            if (organization == null || organization.isEmpty() || organization.equalsIgnoreCase("null")) {
                return "unknown";
            }
            return organization;
        }
        
        // Product varsa organization.product formatında döndür
        if (organization != null && !organization.isEmpty() && !organization.equalsIgnoreCase("null")) {
            return organization + "." + product.substring(product.lastIndexOf('.') + 1);
        }
        
        return product.substring(product.lastIndexOf('.') + 1);
    }

    private boolean isValidProduct(String productName) {
        return productName != null && !productName.isEmpty() && 
               !productName.equalsIgnoreCase("unknown") && 
               !productName.equalsIgnoreCase("null");
    }

    public GraphData<ApplicationEdge> generateConsumerApplicationGraph(
            String consumerOrganizationName,
            String consumerProductName,
            String consumerApplicationName,
            boolean isNgbaOnly) {
        
        // Belirli bir consumer için ilişkileri getir
        List<ApiRelation> relations = apiRelationRepository
            .findByConsumerOrganizationAndConsumerProductAndConsumerApplication(
                consumerOrganizationName, consumerProductName, consumerApplicationName);
        
        Set<String> applicationNames = new HashSet<>();
        Map<String, ApplicationInfo> appInfoMap = new HashMap<>();

        // Consumer application'ı ekle
        String consumerFullName = buildFullName(
            consumerOrganizationName, 
            consumerProductName, 
            consumerApplicationName
        );
        
        applicationNames.add(consumerFullName);
        appInfoMap.put(consumerFullName, new ApplicationInfo(
            consumerOrganizationName,
            consumerProductName,
            consumerApplicationName
        ));

        // Provider application'ları ekle
        relations.forEach(relation -> {
            String providerFullName = buildFullName(
                relation.getProviderOrganization(),
                relation.getProviderProduct(),
                relation.getProviderApplication()
            );
            
            if (!isNgbaOnly || (isNgbaOnly && isValidProduct(relation.getProviderProduct()))) {
                applicationNames.add(providerFullName);
                appInfoMap.put(providerFullName, new ApplicationInfo(
                    relation.getProviderOrganization(),
                    relation.getProviderProduct(),
                    relation.getProviderApplication()
                ));
            }
        });

        // Node'ları oluştur
        List<Node> nodes = applicationNames.stream()
                .map(fullName -> {
                    Node node = new Node();
                    ApplicationInfo appInfo = appInfoMap.get(fullName);
                    
                    if ((appInfo.organizationName == null || appInfo.organizationName.isEmpty() || 
                         appInfo.organizationName.equalsIgnoreCase("null")) &&
                        (appInfo.productName == null || appInfo.productName.isEmpty() || 
                         appInfo.productName.equalsIgnoreCase("null"))) {
                        node.setName(appInfo.applicationName);
                        node.setLabel(appInfo.applicationName);
                    } else {
                        node.setName(fullName);
                        node.setLabel(appInfo.applicationName);
                    }
                    
                    node.setType("application");
                    return node;
                })
                .collect(Collectors.toList());

        // Edge'leri oluştur
        Map<String, ApplicationEdge> edgeMap = new HashMap<>();
        
        relations.forEach(relation -> {
            String targetFullName = buildFullName(
                relation.getProviderOrganization(),
                relation.getProviderProduct(),
                relation.getProviderApplication()
            );

            // isNgbaOnly=true ise ve product null ise bu ilişkiyi atla
            if (isNgbaOnly && !isValidProduct(relation.getProviderProduct())) {
                return;
            }

            String edgeKey = consumerFullName + "->" + targetFullName;
            
            ApplicationEdge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                ApplicationEdge e = new ApplicationEdge();
                e.setSource(consumerFullName);
                e.setTarget(targetFullName);
                e.setDetails(new ArrayList<>());
                return e;
            });
            
            edge.getDetails().addAll(relation.getDetails());
        });

        GraphData<ApplicationEdge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));
        
        return graphData;
    }

    public GraphData<Edge> generateConsumerProductGraph(String consumerOrganizationName, String consumerProductName) {
        // Belirli bir consumer product için ilişkileri getir
        List<ApiRelation> relations = apiRelationRepository
            .findByConsumerOrganizationAndConsumerProduct(consumerOrganizationName, consumerProductName);
        
        Set<String> productNames = new HashSet<>();
        Map<String, Edge> edgeMap = new HashMap<>();

        // Consumer product'ı ekle
        String sourceProduct = buildProductName(consumerOrganizationName, consumerProductName);
        productNames.add(sourceProduct);

        relations.forEach(relation -> {
            String targetProduct = buildProductName(
                relation.getProviderOrganization(), 
                relation.getProviderProduct()
            );
            
            // Source ve target aynı ise veya geçersiz product ise atla
            if (sourceProduct.equals(targetProduct) || !isValidProduct(relation.getProviderProduct())) {
                return;
            }

            productNames.add(targetProduct);

            String edgeKey = sourceProduct + "->" + targetProduct;
            Edge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                Edge e = new Edge();
                e.setSource(sourceProduct);
                e.setTarget(targetProduct);
                e.setApplications(new HashSet<>());
                return e;
            });

            // Application'ı edge'e ekle
            String applicationLabel = relation.getConsumerApplication()
                .substring(relation.getConsumerApplication().lastIndexOf('.') + 1);
            edge.getApplications().add(applicationLabel);
        });

        // Node'ları oluştur
        List<Node> nodes = productNames.stream()
                .map(productName -> {
                    Node node = new Node();
                    node.setName(productName);
                    node.setLabel(productName.substring(productName.lastIndexOf('.') + 1));
                    node.setType("product");
                    return node;
                })
                .collect(Collectors.toList());

        GraphData<Edge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));

        return graphData;
    }

    public GraphData<ApplicationEdge> generateProviderApplicationGraph(
            String providerOrganizationName,
            String providerProductName,
            String providerApplicationName,
            boolean isNgbaOnly) {
        
        // Belirli bir provider için ilişkileri getir
        List<ApiRelation> relations = apiRelationRepository
            .findByProviderOrganizationAndProviderProductAndProviderApplication(
                providerOrganizationName, providerProductName, providerApplicationName);
        
        Set<String> applicationNames = new HashSet<>();
        Map<String, ApplicationInfo> appInfoMap = new HashMap<>();

        // Provider application'ı ekle
        String providerFullName = buildFullName(
            providerOrganizationName, 
            providerProductName, 
            providerApplicationName
        );
        
        applicationNames.add(providerFullName);
        appInfoMap.put(providerFullName, new ApplicationInfo(
            providerOrganizationName,
            providerProductName,
            providerApplicationName
        ));

        // Consumer application'ları ekle
        relations.forEach(relation -> {
            String consumerFullName = buildFullName(
                relation.getConsumerOrganization(),
                relation.getConsumerProduct(),
                relation.getConsumerApplication()
            );
            
            if (!isNgbaOnly || (isNgbaOnly && isValidProduct(relation.getConsumerProduct()))) {
                applicationNames.add(consumerFullName);
                appInfoMap.put(consumerFullName, new ApplicationInfo(
                    relation.getConsumerOrganization(),
                    relation.getConsumerProduct(),
                    relation.getConsumerApplication()
                ));
            }
        });

        // Node'ları oluştur
        List<Node> nodes = applicationNames.stream()
                .map(fullName -> {
                    Node node = new Node();
                    ApplicationInfo appInfo = appInfoMap.get(fullName);
                    
                    if ((appInfo.organizationName == null || appInfo.organizationName.isEmpty() || 
                         appInfo.organizationName.equalsIgnoreCase("null")) &&
                        (appInfo.productName == null || appInfo.productName.isEmpty() || 
                         appInfo.productName.equalsIgnoreCase("null"))) {
                        node.setName(appInfo.applicationName);
                        node.setLabel(appInfo.applicationName);
                    } else {
                        node.setName(fullName);
                        node.setLabel(appInfo.applicationName);
                    }
                    
                    node.setType("application");
                    return node;
                })
                .collect(Collectors.toList());

        // Edge'leri oluştur
        Map<String, ApplicationEdge> edgeMap = new HashMap<>();
        
        relations.forEach(relation -> {
            String consumerFullName = buildFullName(
                relation.getConsumerOrganization(),
                relation.getConsumerProduct(),
                relation.getConsumerApplication()
            );

            // isNgbaOnly=true ise ve product null ise bu ilişkiyi atla
            if (isNgbaOnly && !isValidProduct(relation.getConsumerProduct())) {
                return;
            }

            String edgeKey = consumerFullName + "->" + providerFullName;
            
            ApplicationEdge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                ApplicationEdge e = new ApplicationEdge();
                e.setSource(consumerFullName);
                e.setTarget(providerFullName);
                e.setDetails(new ArrayList<>());
                return e;
            });
            
            edge.getDetails().addAll(relation.getDetails());
        });

        GraphData<ApplicationEdge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));
        
        return graphData;
    }

    public GraphData<Edge> generateProviderProductGraph(String providerOrganizationName, String providerProductName) {
        // Belirli bir provider product için ilişkileri getir
        List<ApiRelation> relations = apiRelationRepository
            .findByProviderOrganizationAndProviderProduct(providerOrganizationName, providerProductName);
        
        Set<String> productNames = new HashSet<>();
        Map<String, Edge> edgeMap = new HashMap<>();

        // Provider product'ı ekle
        String targetProduct = buildProductName(providerOrganizationName, providerProductName);
        productNames.add(targetProduct);

        relations.forEach(relation -> {
            String sourceProduct = buildProductName(
                relation.getConsumerOrganization(), 
                relation.getConsumerProduct()
            );
            
            // Source ve target aynı ise veya geçersiz product ise atla
            if (sourceProduct.equals(targetProduct) || !isValidProduct(relation.getConsumerProduct())) {
                return;
            }

            productNames.add(sourceProduct);

            String edgeKey = sourceProduct + "->" + targetProduct;
            Edge edge = edgeMap.computeIfAbsent(edgeKey, k -> {
                Edge e = new Edge();
                e.setSource(sourceProduct);
                e.setTarget(targetProduct);
                e.setApplications(new HashSet<>());
                return e;
            });

            // Application'ı edge'e ekle
            String applicationLabel = relation.getConsumerApplication()
                .substring(relation.getConsumerApplication().lastIndexOf('.') + 1);
            edge.getApplications().add(applicationLabel);
        });

        // Node'ları oluştur
        List<Node> nodes = productNames.stream()
                .map(productName -> {
                    Node node = new Node();
                    node.setName(productName);
                    node.setLabel(productName.substring(productName.lastIndexOf('.') + 1));
                    node.setType("product");
                    return node;
                })
                .collect(Collectors.toList());

        GraphData<Edge> graphData = new GraphData<>();
        graphData.setNodes(nodes);
        graphData.setEdges(new ArrayList<>(edgeMap.values()));

        return graphData;
    }
} 