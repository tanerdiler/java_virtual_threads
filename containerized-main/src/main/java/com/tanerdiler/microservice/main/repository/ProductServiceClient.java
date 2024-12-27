package com.tanerdiler.microservice.main.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanerdiler.microservice.main.model.Order;
import com.tanerdiler.microservice.main.model.Product;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductServiceClient
{
	private final RestTemplate restTemplate;
	private final RestClient restClient;
	private ObjectMapper om = new ObjectMapper();

	public ProductServiceClient(RestTemplate restTemplate, @Qualifier("productRestClient") RestClient restClient) {
		this.restTemplate = restTemplate;
		this.restClient = restClient;
	}

	public Product findByIdByRestTemplate(Integer productId) {
		var url = "http://localhost:8083/product/api/v1/products/%d".formatted(productId);
		return restTemplate.getForEntity(url, Product.class).getBody();
	}

	public List<Product> findAllByRestTemplate() {
		var url = "http://localhost:8083/product/api/v1/products";
		return Arrays.asList(restTemplate.getForEntity(url, Product[].class).getBody());
	}

	public Product findByIdByRestClient(Integer productId) {
		return restClient.get().uri("/products/%d".formatted(productId)).retrieve().toEntity(Product.class).getBody();
	}

	public List<Product> findAllByRestClient() {
		return Arrays.asList(restClient.get().uri("/products").retrieve().toEntity(Product[].class).getBody());
	}


	@SneakyThrows
	public Product findByIdByHttpClient4(Integer productId) {
		try(var client = HttpClientBuilder.create().build()) {
			try (var response = client.execute(new HttpGet("http://localhost:8083/product/api/v1/products/%d".formatted(productId)))) {
				return om.readValue(EntityUtils.toString(response.getEntity()), Product.class);
			}
		}
	}

	@SneakyThrows
	public List<Product> findAllByHttpClient4() {
		try(var client = HttpClientBuilder.create().build()) {
			try (var response = client.execute(new HttpGet("http://localhost:8083/product/api/v1/products"))) {
				return om.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<List<Product>>(){});
			}
		}
	}

}
