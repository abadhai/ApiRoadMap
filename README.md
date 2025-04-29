# 🧵 Building a Long-Running Async REST API in Spring Boot (202 + 303 Pattern)

> Most HTTP APIs are expected to respond quickly. But what if your process takes longer — say, several seconds or even minutes? Returning a simple `200 OK` doesn’t quite cut it. That’s where this elegant long-running request pattern comes in.

---

## ✅ What We’ll Build

A Spring Boot API that:

- Accepts an order creation request
- Responds immediately with `202 Accepted` and a status location
- Allows the client to poll for status
- Redirects to the final result when the resource is ready using `303 See Other`

Perfect for:
- Background jobs
- Asynchronous order processing
- External system integrations

---

## 🧱 Key Concepts

### 🔄 HTTP Status Codes

| Status | Description |
|--------|-------------|
| `202 Accepted` | The request is valid and has been accepted for processing, but it's not done yet. |
| `303 See Other` | The resource has finished processing. Redirect the client to the final location. |

---

## 🚀 API Flow

1. **Client:** `POST /api/orders/create?orderId=99`  
   - Server: returns `202 Accepted` + `Location: /orders/status/99`

2. **Client:** `GET /api/orders/status/99`  
   - If processing: `202 Accepted`  
   - If done: `303 See Other` + `Location: /api/orders/99`

3. **Client:** `GET /api/orders/99`  
   - Server: returns final result

This pattern avoids blocking clients and provides a RESTful way to handle long-running operations.

---

## 🧩 Implementation in Spring Boot

```java
@RestController
@RequestMapping("/api/orders")
public class AsyncLongRequestController {

    private final Map<Long, String> orderStatus = new ConcurrentHashMap<>();

    @PostMapping("/create")
    public ResponseEntity<Void> createOrder(@RequestParam Long orderId) {
        orderStatus.put(orderId, "Processing");

        // Simulate long task
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000); // 10-second simulated delay
                orderStatus.put(orderId, "Completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return ResponseEntity
            .accepted()
            .header(HttpHeaders.LOCATION, "/orders/status/" + orderId)
            .build();
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<Object> getOrderStatus(@PathVariable Long orderId) {
        String status = orderStatus.get(orderId);
        if (status == null) return ResponseEntity.notFound().build();

        if ("Completed".equals(status)) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header(HttpHeaders.LOCATION, "/api/orders/" + orderId)
                    .body(Map.of("status", "Completed"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("status", "Processing"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<String> getFinalOrder(@PathVariable Long orderId) {
        if (!orderStatus.containsKey(orderId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Order " + orderId + " is ready!");
    }

    @GetMapping("/test")
    public String longResourceTest() {
        return "Hello World!";
    }
}
