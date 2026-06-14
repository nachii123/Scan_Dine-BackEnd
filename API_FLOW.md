# Scan Dine API Flow

## 1. Customer Journey

1. Customer registers or logs in through `POST /api/auth/register`, `POST /api/auth/login`, or OTP endpoints.
2. Customer opens restaurant menu through `GET /api/v1/restaurants/{restaurantId}/menu`.
3. Customer scans table with `POST /api/v1/tables/scan`.
4. Customer places an order with `POST /api/v1/tables/order`.
5. Customer checks order state with `GET /api/v1/tables/session/{sessionId}/orders` or `GET /api/v1/tables/orders/{orderId}`.
6. Customer requests bill or calls waiter through the session actions.
7. Customer rates the completed order with `POST /api/v1/tables/orders/{orderId}/rate`.
8. Customer reorders from a previous order with `POST /api/v1/tables/reorder`.
9. Customer books a reservation with `POST /api/v1/tables/reservations`.

## 2. Restaurant Setup

1. Restaurant is onboarded through `POST /api/v1/restaurants/onboard`.
2. Restaurant metadata is updated through `PUT /api/v1/restaurants/{restaurantId}`.
3. Menu categories are created through `POST /api/v1/restaurants/menu/categories`.
4. Menu items are created through `POST /api/v1/restaurants/menu/items`.
5. Menu visibility and availability are controlled from captain endpoints.

## 3. Captain Operations

1. Captain registers or logs in through `POST /api/captain/auth/register` and `POST /api/captain/auth/login`.
2. Captain creates and manages tables under `/api/v1/captain/tables`.
3. Captain opens or clears a table session.
4. Captain accepts, prepares, rejects, serves, or adds items to orders.
5. Captain views KOT, bill, kitchen display, daily sales, peak hours, and ratings.
6. Captain manages reservations for the restaurant.

## 4. Important Runtime Dependencies

- JWT authentication is required for customer order actions and captain actions.
- Restaurant and table data are split across separate datasource configs.
- Order state changes depend on table session state.
- Bill and kitchen views derive from the current order and session history.

## 5. Known Gaps

- No websocket or SSE layer for live updates.
- No payment lifecycle.
- No reservation conflict logic beyond a simple time-window guard.
- No explicit order event history.
- No idempotency keys for retry-safe order submission.

## 6. Suggested Code Layout

```text
com.example.scan_dineCustomer
  auth
    controller
    service
  customer
    controller
  restaurant
    controller
    service
  table
    controller
    service
  dto
  config
  entity
  repo
  util
```
