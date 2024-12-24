package com.tanerdiler.microservice.main.repository;

import com.tanerdiler.microservice.main.model.Account;
import com.tanerdiler.microservice.main.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final RestClient restClient;

    public OrderServiceClient(RestTemplate restTemplate, @Qualifier("orderRestClient") RestClient restClient) {
        this.restTemplate = restTemplate;
        this.restClient = restClient;
    }

    public Order findByIdByRestTemplate(Integer orderId) {
        var url = "http://localhost:8082/order/api/v1/orders/%d".formatted(orderId);
        return restTemplate.getForEntity(url, Order.class).getBody();
    }

    public List<Order> findAllByRestTemplate() {
        var url = "http://localhost:8082/order/api/v1/orders";
        return Arrays.asList(restTemplate.getForEntity(url, Order[].class).getBody());
    }

    public Order findByIdByRestClient(Integer orderId) {
        return restClient.get().uri("/orders/%d".formatted(orderId)).retrieve().toEntity(Order.class).getBody();
    }

    public List<Order> findAllByRestClient() {
        return Arrays.asList(restClient.get().uri("/orders").retrieve().toEntity(Order[].class).getBody());
    }
}
