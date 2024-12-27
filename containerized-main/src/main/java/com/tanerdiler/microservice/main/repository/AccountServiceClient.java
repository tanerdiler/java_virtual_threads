package com.tanerdiler.microservice.main.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanerdiler.microservice.main.model.Account;
import com.tanerdiler.microservice.main.model.Product;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AccountServiceClient
{
	private final RestTemplate restTemplate;
	private final RestClient restClient;
	private ObjectMapper om = new ObjectMapper();

	public AccountServiceClient(RestTemplate restTemplate, @Qualifier("accountRestClient") RestClient restClient) {
		this.restTemplate = restTemplate;
		this.restClient = restClient;
	}

	public Account findByIdByRestTemplate(Integer accountId) {
		var url = "http://localhost:8081/account/api/v1/accounts/%d".formatted(accountId);
		return restTemplate.getForEntity(url, Account.class).getBody();
	}

	public List<Account> findAllByRestTemplate() {
		var url = "http://localhost:8081/account/api/v1/accounts";
		return Arrays.asList(restTemplate.getForEntity(url, Account[].class).getBody());
	}

	public Account findByIdByRestClient(Integer productId) {
		return restClient.get().uri("/accounts/%d".formatted(productId)).retrieve().toEntity(Account.class).getBody();
	}

	public List<Account> findAllByRestClient() {
		return Arrays.asList(restClient.get().uri("/accounts").retrieve().toEntity(Account[].class).getBody());
	}

	@SneakyThrows
	public Account findByIdByHttpClient4(Integer productId) {
		try(var client = HttpClientBuilder.create().build()) {
			try (var response = client.execute(new HttpGet("http://localhost:8081/account/api/v1/accounts/%d".formatted(productId)))) {
				return om.readValue(EntityUtils.toString(response.getEntity()), Account.class);
			}
		}
	}

	@SneakyThrows
	public List<Account> findAllByHttpClient4() {
		try(var client = HttpClientBuilder.create().build()) {
			try (var response = client.execute(new HttpGet("http://localhost:8081/account/api/v1/accounts"))) {
				return om.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<List<Account>>(){});
			}
		}
	}

}
