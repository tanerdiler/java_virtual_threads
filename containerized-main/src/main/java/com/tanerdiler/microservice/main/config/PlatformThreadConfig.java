package com.tanerdiler.microservice.main.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@Profile("platform")
public class PlatformThreadConfig {

    @Bean("orderRestClient")
    public RestClient getOrderRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8082/order/api/v1")
                .build();
    }

    @Bean("productRestClient")
    public RestClient getProductRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8083/product/api/v1")
                .build();
    }

    @Bean("accountRestClient")
    public RestClient getAccountRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8081/account/api/v1")
                .build();
    }
/*
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadFactory factory = Thread.ofVirtual().name("VirtualThread").factory();
        return new TaskExecutorAdapter(Executors.newThreadPerTaskExecutor(factory));
        //return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            ThreadFactory factory = Thread.ofVirtual().name("VirtualThread").factory();
            protocolHandler.setExecutor(Executors.newThreadPerTaskExecutor(factory));
            //protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }*/
}
