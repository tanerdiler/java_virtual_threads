package com.tanerdiler.microservice.main.resource;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static java.util.stream.Collectors.toMap;
import com.tanerdiler.microservice.main.dto.OrderDTO;
import com.tanerdiler.microservice.main.model.Account;
import com.tanerdiler.microservice.main.model.Order;
import com.tanerdiler.microservice.main.model.Product;
import com.tanerdiler.microservice.main.repository.AccountServiceClient;
import com.tanerdiler.microservice.main.repository.OrderServiceClient;
import com.tanerdiler.microservice.main.repository.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

@Slf4j
@RestController
@RequestMapping("/api/v1/backoffice")
@RequiredArgsConstructor
public class BackofficeRestClientController {

    private final ProductServiceClient productService;
    private final OrderServiceClient orderService;
    private final AccountServiceClient accountService;

    private static final String MONITOR = "SYNCHRONIZED";

    @GetMapping("/orders/byRestClient")
    public ResponseEntity<List<OrderDTO>> getOrders(
            @RequestParam(value = "concurrency", defaultValue = "sequential") String concurrency) throws InterruptedException, ExecutionException {

        List<Order> orders;
        Map<Integer, Product> products;
        Map<Integer, Account> accounts;

        if ("sequential".equalsIgnoreCase(concurrency)) {
            orders = orderService.findAllByRestClient();
            products = productService.findAllByRestClient().stream().collect(toMap(p -> p.getId(), p -> p));
            accounts = accountService.findAllByRestClient().stream().collect(toMap(a -> a.getId(), a -> a));
        } else if ("scopeFork".equalsIgnoreCase(concurrency)) {
            try (StructuredTaskScope.ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {
                StructuredTaskScope.Subtask<List<Order>> ordersTask = scope.fork(() -> {
                    log.debug("OrderScope Thread : {}", Thread.currentThread());
                    return orderService.findAllByRestTemplate();
                });
                StructuredTaskScope.Subtask<Map<Integer, Product>> productsTask = scope.fork(() -> {
                    log.debug("ProductScope Thread : {}", Thread.currentThread());
                    return productService.findAllByRestTemplate().stream().collect(toMap(p -> p.getId(), p -> p));
                });
                StructuredTaskScope.Subtask<Map<Integer, Account>> accountsTask = scope.fork(() -> {
                    log.debug("AccountScope Thread : {}", Thread.currentThread());
                    return accountService.findAllByRestTemplate().stream().collect(toMap(a -> a.getId(), a -> a));
                });

                scope.join();
                orders = ordersTask.get();
                products = productsTask.get();
                accounts = accountsTask.get();
            }
        } else if ("completableFuture".equalsIgnoreCase(concurrency)) {
            log.debug("completableFuture Thread : {}", Thread.currentThread());
            var orderFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("OrderFuture Thread : {}", Thread.currentThread());
                return orderService.findAllByRestClient();
            });
            var productFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("ProductFuture Thread : {}", Thread.currentThread());
                return productService.findAllByRestClient().stream().collect(toMap(p -> p.getId(), p -> p));
            });
            var accountFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("AccountFuture Thread : {}", Thread.currentThread());
                return accountService.findAllByRestClient().stream().collect(toMap(a -> a.getId(), a -> a));
            });

            CompletableFuture.allOf(orderFuture, productFuture, accountFuture).join();

            orders = orderFuture.get();
            products = productFuture.get();
            accounts = accountFuture.get();
        } else if ("completableFutureWithVirtualTaskExecutor".equalsIgnoreCase(concurrency)) {
            log.debug("completableFuture Thread : {}", Thread.currentThread());
            var executor = Executors.newVirtualThreadPerTaskExecutor();
            var orderFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("OrderFuture Thread : {}", Thread.currentThread());
                return orderService.findAllByRestClient();
            }, executor);
            var productFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("ProductFuture Thread : {}", Thread.currentThread());
                return productService.findAllByRestClient().stream().collect(toMap(p -> p.getId(), p -> p));
            }, executor);
            var accountFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("AccountFuture Thread : {}", Thread.currentThread());
                return accountService.findAllByRestClient().stream().collect(toMap(a -> a.getId(), a -> a));
            }, executor);

            CompletableFuture.allOf(orderFuture, productFuture, accountFuture).join();

            orders = orderFuture.get();
            products = productFuture.get();
            accounts = accountFuture.get();
        } else if ("completableFutureWithThreadTaskExecutor".equalsIgnoreCase(concurrency)) {
            log.debug("completableFuture Thread : {}", Thread.currentThread());
            var executor = Executors.newThreadPerTaskExecutor(Thread::new);
            var orderFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("OrderFuture Thread : {}", Thread.currentThread());
                return orderService.findAllByRestClient();
            }, executor);
            var productFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("ProductFuture Thread : {}", Thread.currentThread());
                return productService.findAllByRestClient().stream().collect(toMap(p -> p.getId(), p -> p));
            }, executor);
            var accountFuture = CompletableFuture.supplyAsync(() -> {
                log.debug("AccountFuture Thread : {}", Thread.currentThread());
                return accountService.findAllByRestClient().stream().collect(toMap(a -> a.getId(), a -> a));
            }, executor);

            CompletableFuture.allOf(orderFuture, productFuture, accountFuture).join();

            orders = orderFuture.get();
            products = productFuture.get();
            accounts = accountFuture.get();
        } else if ("pinnedCall".equalsIgnoreCase(concurrency)) {
            synchronized (MONITOR) {
                try (StructuredTaskScope.ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    StructuredTaskScope.Subtask<List<Order>> ordersTask = scope.fork(() -> {
                        log.debug("OrderScope Thread : {}", Thread.currentThread());
                        return orderService.findAllByRestTemplate();
                    });
                    StructuredTaskScope.Subtask<Map<Integer, Product>> productsTask = scope.fork(() -> {
                        log.debug("ProductScope Thread : {}", Thread.currentThread());
                        return productService.findAllByRestTemplate().stream().collect(toMap(p -> p.getId(), p -> p));
                    });
                    StructuredTaskScope.Subtask<Map<Integer, Account>> accountsTask = scope.fork(() -> {
                        log.debug("AccountScope Thread : {}", Thread.currentThread());
                        return accountService.findAllByRestTemplate().stream().collect(toMap(a -> a.getId(), a -> a));
                    });

                    scope.join();
                    orders = ordersTask.get();
                    products = productsTask.get();
                    accounts = accountsTask.get();
                }
            }
        } else {
            orders = EMPTY_LIST;
            products = EMPTY_MAP;
            accounts = EMPTY_MAP;
        }

        List<OrderDTO> orderDTOList = new ArrayList<>();
        orders.forEach(o -> {
            orderDTOList.add(new OrderDTO(
                    o.getId(),
                    o.getCount(),
                    o.getPrice(),
                    o.getDiscountedPrice(),
                    accounts.get(o.getAccountId()).getFullname(),
                    products.get(o.getProductId()).getName()
                    //"UNKNOWN",
                    //"UNIDENTIFIED"
            ));
        });
        log.warn("Fetched all orders... Thread -> {}", Thread.currentThread());
        return ResponseEntity.ok(orderDTOList);

    }

}
