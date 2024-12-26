# Java Virtual Thread Benchmark On Spring Boot

![Service Graph](https://github.com/tanerdiler/java_virtual_threads/blob/master/assets/service_map.png)

## Benchmark Results

Tomcat Thread Count : 10
Request Number      : 1000 
Concurrency Level   : 200

**Spring Boot Profile  : platform**

| Concurrency       | Executor                        |   Req./Sec. | Total Time (sec) |
|-------------------|:--------------------------------|------------:|-----------------:|
| Sequential        |                                 |        2,45 |              407 |
| CompletableFuture | ForkJoinPool(Auto)              |        1.73 |              576 |
| CompletableFuture | newThreadPerTaskExecutor        |        4,93 |              203 |
| CompletableFuture | newVirtualThreadPerTaskExecutor |        4,93 |              203 |
| ScopeFork         |                                 |        4,91 |              204 |


**Spring Boot Profile  : virtual**

| Concurrency       | Executor                        | Req./Sec. | Total Time (sec) |
|-------------------|:--------------------------------|----------:|-----------------:|
| Sequential        |                                 |     40,06 |               25 |
| CompletableFuture | ForkJoinPool(Auto)              |      1.73 |              576 |
| CompletableFuture | newThreadPerTaskExecutor        |     73,27 |               14 |
| CompletableFuture | newVirtualThreadPerTaskExecutor |     74,39 |               13 |
| ScopeFork         |                                 |     75,39 |               13 |

## Spring Boot Configuration
```
server:
  tomcat.threads.max: 10
  ...
  ...
---

spring:
  config:
    activate:
      on-profile: "platform"
  threads:
    virtual:
      enabled: false
---

spring:
  config:
    activate:
      on-profile: "virtual"
  threads:
    virtual:
      enabled: true
```
## Commands

```
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-products.jar
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-orders.jar
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-accounts.jar
```

```
java -jar --enable-preview -Dspring.profiles.active=virtual target/containerized-main.jar
java -jar --enable-preview -Dspring.profiles.active=platform target/containerized-main.jar
```

```
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFuture
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=sequential
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFutureWithVirtualTaskExecutor
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=completableFutureWithThreadTaskExecutor
ab -n 1000 -c 200 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate?concurrency=scopeFork
```

## Virtual Thread JVM Params:

```
-Djdk.virtualThreadScheduler.parallelism=1
-Djdk.virtualThreadScheduler.maxPoolSize=1
-Djdk.virtualThreadScheduler.minRunnable=1
-Djdk.tracePinnedThreads=full
```
## Resources

1. [Virtual Threads in Java – Deep Dive with Examples ](https://www.youtube.com/watch?v=9dUPPHREF7w)
2. [Spring Boot 3.2 With Virtual Threads Explained | Benchmarking Insights | JavaTechie ](https://www.youtube.com/watch?v=tykrCxwmMG4)
3. [Virtual Threads - Strengths and Pitfalls with Victor Rentea ](https://dzone.com/articles/demystifying-virtual-thread-performance-unveiling)
4. [Java Virtual Threads — some early gotchas to look out for! ](https://medium.com/@phil_3582/java-virtual-threads-some-early-gotchas-to-look-out-for-f65df1bad0db)
5. [Pinning: A pitfall to avoid when using virtual threads in Java ](https://abhishekvrshny.medium.com/pinning-a-pitfall-to-avoid-when-using-virtual-threads-in-java-482c5eab78a3)
6. [Java Virtual Thread Pinning • Todd Ginsberg ](https://todd.ginsberg.com/post/java/virtual-thread-pinning/)
7. [Apache HttpClient 5.4 Compatibility with Java Virtual Threads and Java 21 Runtime.](https://downloads.apache.org/httpcomponents/httpclient/RELEASE_NOTES-5.4.x.txt)
8. [Curiosities of Java Virtual Threads pinning with synchronized ](https://mikemybytes.com/2024/02/28/curiosities-of-java-virtual-threads-pinning-with-synchronized/)
9. [The Ultimate Guide to Java Virtual Threads ](https://rockthejvm.com/articles/the-ultimate-guide-to-java-virtual-threads)
10. [Beyond Loom: Weaving new concurrency patterns | Red Hat Developer](https://developers.redhat.com/articles/2023/10/03/beyond-loom-weaving-new-concurrency-patterns#)
11. [Spring Tips: Virtual Threads](https://www.youtube.com/watch?v=9iH5h11YJak)
12. https://github.com/spring-projects/spring-boot/issues/41937
