package com.tanerdiler.microservice.main.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class Config {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {

        Integer timeoutPeriod = 15000;
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(timeoutPeriod, TimeUnit.MILLISECONDS)
                .setSocketTimeout(timeoutPeriod, TimeUnit.MILLISECONDS)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(500);
        cm.setDefaultConnectionConfig(connConfig);

        var client = HttpClients.custom().setConnectionManager(cm).build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);
        factory.setConnectTimeout(timeoutPeriod);
        factory.setConnectionRequestTimeout(timeoutPeriod);

        return factory;
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
