### Spring Boot Profiles:


Request Number    : 1000 
Concurrency Level : 200

| Concurrency       | Executor                        |   Req./Sec. | Total Time (sec) |
|-------------------|:--------------------------------|------------:|-----------------:|
| Sequential        |                                 |       40.06 |               25 |
| CompletableFuture | ForkJoinPool(Auto)              |        1.73 |              576 |
| CompletableFuture | newThreadPerTaskExecutor        |       74.55 |               13 |
| CompletableFuture | newVirtualThreadPerTaskExecutor |       75.27 |               13 |
| ScopeFork         |                                 |       75.13 |               13 |

### Commands

`java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-products.jar
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-orders.jar
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-accounts.jar`

`java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-main.jar
java -jar --enable-preview -Dspring.profiles.active=platform target/containerized-main.jar`

`ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFuture
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=sequential
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFutureWithVirtualTaskExecutor
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFutureWithThreadTaskExecutor
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=scopeFork`

### Virtual Thread JVM Params:

-Djdk.virtualThreadScheduler.parallelism=1
-Djdk.virtualThreadScheduler.maxPoolSize=1
-Djdk.virtualThreadScheduler.minRunnable=1
-Djdk.tracePinnedThreads=full