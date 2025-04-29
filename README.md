# ApiRoadMap
# 🧵 Async Long-Running Request API

This Spring Boot application demonstrates how to implement a **long-running asynchronous REST API** using HTTP status codes like `202 Accepted`, `303 See Other`, and a status polling mechanism.

## 📦 Features

- Create long-running orders using `POST /api/orders/create`.
- Poll order status until completed using `GET /api/orders/status/{orderId}`.
- Retrieve final order result via `GET /api/orders/{orderId}`.
- Asynchronous background simulation using `CompletableFuture`.

---

## 🚀 API Endpoints

### 1. Health Check

