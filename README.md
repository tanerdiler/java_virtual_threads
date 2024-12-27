# Java Virtual Thread Benchmark On Spring Boot

![Service Graph](https://github.com/tanerdiler/java_virtual_threads/blob/master/assets/service-graph.png)

## Benchmark Results
```
Tomcat Thread Count  : 10
Request Number       : 1000 
Concurrency Level    : 200
Http Conn. Pool Size : 500
```
**Spring Boot Profile  : platform**

| Concurrency       | Executor                        | Req./Sec. | Total Time (sec) |
|-------------------|:--------------------------------|----------:|-----------------:|
| Sequential        |                                 |      1,64 |              609 |
| CompletableFuture | ForkJoinPool**                  |      1.73 |              576 |
| CompletableFuture | newThreadPerTaskExecutor        |      4,93 |              203 |
| CompletableFuture | newVirtualThreadPerTaskExecutor |      4,91 |              204 |
| ScopeFork         |                                 |      4,93 |              202 |


**Spring Boot Profile  : virtual**

| Concurrency       | Executor                        | Req./Sec. |      Total Time (sec) |
|-------------------|:--------------------------------|----------:|----------------------:|
| Sequential        |                                 |     27.39 |                    36 |
| CompletableFuture | ForkJoinPool**                  |      1.16 |                   863 |
| CompletableFuture | newThreadPerTaskExecutor        |     66.58 |                    15 |
| CompletableFuture | newVirtualThreadPerTaskExecutor |     67.20 |                    15 |
| ScopeFork         |                                 |     66.93 |                    15 |


**ForkJoinPool**** : The same ForkJoinPool utilized by Spring Boot for request handling is also being used to execute virtual threads. See ***Isolate Virtual Thread Pools*** section.

## Connection Pool Effect
```
Tomcat Thread Count : 10
Request Number      : 50
Concurrency Level   : 10
Spring Boot Profile : virtual
Concurrency Method  : completableFutureWithVirtualTaskExecutor
```
```
ab -n 50 -c 10 http://localhost:2222/backoffice/api/v1/backoffice/orders/byRestTemplate\?concurrency\=completableFutureWithVirtualTaskExecutor
```

| Pool Size | Req./Sec. |      Total Time (sec) |
|-----------|----------:|----------------------:|
| 100       |      3.96 |                12.621 |
| 50        |      3.92 |                12.758 |
| 25        |      3.33 |                14.993 |
| 10        |      1.53 |                32.663 |

Please see ***Using Connection Pools with Java Virtual Threads*** section.

## Reason of Performance Lost Using CompletableFuture In Virtual Thread

Using CompletableFuture inside a Spring Boot REST controller with Virtual Threads enabled might lead to poor performance due to misalignment between CompletableFuture's execution model and Virtual Threads' characteristics. Here are the key reasons and considerations:

**1. Default Executor of CompletableFuture**

  - By default, CompletableFuture uses the ForkJoinPool.commonPool for its execution.
  - Virtual Threads rely on their own scheduling mechanism and are designed to be lightweight and non-blocking. However, the common pool is optimized for platform threads and computational tasks, not Virtual Threads.
  - When you use CompletableFuture without specifying an executor, tasks are scheduled on the common pool, which can become a bottleneck, especially in high-concurrency scenarios.

**2. Thread Blocking in CompletableFuture**
   - If your CompletableFuture tasks involve blocking I/O operations (e.g., database queries, HTTP calls), those blocks are executed on the ForkJoinPool threads.
   - This reduces the effectiveness of Virtual Threads because blocking operations in the ForkJoinPool are not optimized for the cooperative scheduling of Virtual Threads.

**3. Overhead of Context Switching**
   - Virtual Threads aim to minimize context switching overhead. However, when CompletableFuture schedules tasks in the common pool, it might cause additional context switches between Virtual Threads and platform threads, leading to performance degradation.

**4. Inefficient Use of Virtual Threads**
   - Virtual Threads are best utilized when running directly without relying on traditional thread-pooling mechanisms.
   - Wrapping tasks in CompletableFuture adds an extra layer of abstraction, which might interfere with the natural scheduling of Virtual Threads.

### Recommendations to Improve Performance
**1. Use Virtual Thread Executor Explicitly:**

Instead of using the default executor, configure CompletableFuture to use a Virtual Thread Executor:
```
Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

CompletableFuture.supplyAsync(() -> {
// Task logic here
return result;
}, virtualThreadExecutor);
```

**2. Avoid CompletableFuture When Not Necessary:**

If your REST controller already benefits from Virtual Threads (e.g., blocking code runs efficiently on Virtual Threads), avoid wrapping tasks in CompletableFuture unnecessarily.

**3.Profile the Application:**

- Use tools like jconsole, jvisualvm, or thread dump analysis to identify bottlenecks.
- Ensure that tasks are not accidentally executed on the ForkJoinPool.commonPool.

**4. Rewrite Tasks for Virtual Threads:**

Leverage Virtual Threads directly for blocking operations instead of offloading them to CompletableFuture:
```
@GetMapping("/endpoint")
public ResponseEntity<String> endpoint() {
// Direct use of Virtual Threads
return ResponseEntity.ok(someBlockingTask());
}
```

**5. Optimize I/O Operations:**

Ensure that any blocking I/O (e.g., database calls, HTTP requests) is compatible with Virtual Threads to take full advantage of their cooperative blocking mechanism.

### Key Insight:
The poor performance arises because CompletableFuture's default behavior doesn't align well with Virtual Threads. By explicitly using a Virtual Thread Executor or avoiding unnecessary abstraction, you can achieve significantly better performance.

---

## Isolate Virtual Thread Pools

If the same ForkJoinPool being used by Spring Boot's request handlers is also carrying virtual threads, it might lead to potential contention and reduced performance. This is because the ForkJoinPool is optimized for task-stealing and computational tasks, not for managing virtual threads, which behave differently.

### How Virtual Threads Are Managed

Virtual threads rely on carrier threads (often drawn from a pool like ForkJoinPool.commonPool()) for execution. If the common pool is shared between virtual threads and Spring Boot's request-handling tasks, it can create bottlenecks. Virtual threads are designed to block without holding carrier threads, but contention on the shared pool could still impact performance.

### Best Practices to Address This

1. Isolate Thread Pools for Virtual Threads: Ensure that virtual threads use their dedicated thread pool rather than sharing the same ForkJoinPool used by Spring Boot. 
2. Customize Spring Boot's Thread Pool: Configure Spring Boot to use a separate executor for handling web requests, freeing up the common pool for virtual threads. 
3. Explicitly Use a Dedicated Executor: Use Executors.newVirtualThreadPerTaskExecutor() for virtual thread tasks.

### Configuring Separate Executors
***1. Customize Spring Boot Thread Pool:***
   Spring Boot uses a TaskExecutor for handling requests. Override the default executor in your Spring configuration:

```
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SpringBootConfig {
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("SpringBoot-");
        executor.initialize();
        return executor;
    }
}
```
This ensures that Spring Boot’s request handlers use a separate thread pool.

***2. Use Virtual Threads Explicitly:***
   For workloads leveraging virtual threads, use a dedicated executor:

```
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadExecutor {
    public static void main(String[] args) {
        // Create a dedicated virtual thread executor
        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

        virtualThreadExecutor.submit(() -> {
            System.out.println("Running in a virtual thread: " + Thread.currentThread());
        });

        virtualThreadExecutor.shutdown();
    }
}
```

### Why Isolation Matters
- **Avoid Contention:** Separating workloads prevents blocking operations in virtual threads from impacting Spring Boot's request-handling efficiency.
- **Optimize Performance:** Each thread pool can be tuned for the specific nature of its tasks (compute-heavy vs. I/O-heavy).

By isolating the ForkJoinPool used by Spring Boot and creating dedicated executors for virtual threads, you ensure optimal performance and scalability in a mixed workload application.

---

## Using Connection Pools with Java Virtual Threads
While Java Virtual Threads enable highly scalable and lightweight concurrency, traditional connection pooling mechanisms can impose limitations that negate some of their benefits:

**1. Bottleneck on Connections:**

- Connection pools typically maintain a fixed or limited number of connections to a resource (e.g., database, API). Virtual threads, designed to handle millions of concurrent tasks, may become constrained by these limits, reducing throughput.

**2. Idle Connection Management Overhead:**

- Connection pools often include mechanisms for managing idle connections, such as periodic eviction or validation. This can introduce unnecessary delays or overhead when virtual threads make frequent, short-lived requests.

**3. Underutilization of Virtual Threads:**

- Virtual threads rely on carrier threads to execute tasks. If the connection pool blocks threads waiting for available connections, the virtual threads may remain idle, underutilizing the system’s concurrency potential.

**4. Contention and Latency:**

- When multiple virtual threads compete for a limited pool of connections, contention can increase latency, leading to performance degradation in high-concurrency scenarios.

**5. Inefficiency in Resource Scaling:**

- Traditional connection pools are not designed to scale dynamically with the high concurrency levels virtual threads provide. This mismatch can lead to resource inefficiency and suboptimal performance.

### Recommendations to Mitigate These Effects:

**1. Use Connectionless Models:**

- Opt for libraries or designs that establish direct connections on demand rather than relying on a pooled approach, especially for short-lived or high-throughput workloads.

**2. Increase Pool Size Dynamically:**

- Configure connection pools to scale with the workload, ensuring that the maximum connection count aligns with the virtual thread demands.

**3. Leverage Asynchronous I/O:**

- Combine virtual threads with non-blocking or asynchronous I/O mechanisms to minimize contention for pooled connections.

**4. Monitor and Profile:**

- Continuously monitor the interaction between virtual threads and the connection pool to identify bottlenecks and adjust configurations accordingly.

By addressing these challenges, developers can fully exploit the performance and scalability benefits of Java Virtual Threads while minimizing the negative effects of connection pooling.

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
