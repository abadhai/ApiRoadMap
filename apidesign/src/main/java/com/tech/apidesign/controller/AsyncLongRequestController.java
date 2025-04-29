package com.tech.apidesign.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/orders")
public class AsyncLongRequestController {

    @GetMapping("/test")
    String longResourceTest(){

        return "Hello World!";
    }

    private final Map<Long, String> orderStatus = new ConcurrentHashMap<>();

    @PostMapping("/create")
    public ResponseEntity<Void> createOrder(@RequestParam Long orderId) {
        orderStatus.put(orderId, "Processing");

        // Simulate async processing
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000); // Simulate long processing (5 seconds)
                orderStatus.put(orderId, "Completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Return 202 Accepted + Location header
        return ResponseEntity
                .accepted()
                .header(HttpHeaders.LOCATION, "/orders/status/" + orderId)
                .build();
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<Object> getOrderStatus(@PathVariable Long orderId) {
        String status = orderStatus.get(orderId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        if ("Completed".equals(status)) {
            // Redirect to the final resource
            return ResponseEntity.status(HttpStatus.SEE_OTHER) // 303
                    .header(HttpHeaders.LOCATION, "/api/orders/" + orderId)
                    .body(Map.of("status", "Completed"));
        }

        // Still processing
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
}
