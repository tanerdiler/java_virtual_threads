package com.tanerdiler.microservice.main.repository;

import com.tanerdiler.microservice.main.model.Account;
import com.tanerdiler.microservice.main.model.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class AccountServiceClient
{
	private final RestTemplate restTemplate;
	private final RestClient restClient;

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

}
