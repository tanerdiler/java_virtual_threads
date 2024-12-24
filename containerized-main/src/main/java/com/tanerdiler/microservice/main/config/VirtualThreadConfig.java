package com.tanerdiler.microservice.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Profile("virtual")
public class VirtualThreadConfig {

    @Bean("orderRestClient")
    public RestClient getOrderRestClient(RestClient.Builder builder, ExecutorService virtualThreadExecutor) {
        return builder
                .baseUrl("http://localhost:8082/order/api/v1")
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder()
                                .executor(virtualThreadExecutor)  // Configure to use virtual threads
                                .build()))
                .build();
    }

    @Bean("productRestClient")
    public RestClient getProductRestClient(RestClient.Builder builder, ExecutorService virtualThreadExecutor) {
        return builder
                .baseUrl("http://localhost:8083/product/api/v1")
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder()
                                .executor(virtualThreadExecutor)  // Configure to use virtual threads
                                .build()))
                .build();
    }

    @Bean("accountRestClient")
    public RestClient getAccountRestClient(RestClient.Builder builder, ExecutorService virtualThreadExecutor) {
        return builder
                .baseUrl("http://localhost:8081/account/api/v1")
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder()
                                .executor(virtualThreadExecutor)  // Configure to use virtual threads
                                .build()))
                .build();
    }

    @Bean
    public ExecutorService getVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
