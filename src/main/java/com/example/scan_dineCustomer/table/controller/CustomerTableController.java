package com.example.scan_dineCustomer.table.controller;

import com.example.scan_dineCustomer.table.dto.EstimatedWaitResponse;
import com.example.scan_dineCustomer.table.dto.OrderRatingRequest;
import com.example.scan_dineCustomer.table.dto.OrderRatingResponse;
import com.example.scan_dineCustomer.table.dto.OrderResponse;
import com.example.scan_dineCustomer.table.dto.PlaceOrderRequest;
import com.example.scan_dineCustomer.table.dto.ReorderRequest;
import com.example.scan_dineCustomer.table.dto.ReservationRequest;
import com.example.scan_dineCustomer.table.dto.ReservationResponse;
import com.example.scan_dineCustomer.table.dto.ScanTableRequest;
import com.example.scan_dineCustomer.table.dto.SessionResponse;
import com.example.scan_dineCustomer.table.dto.TableResponse;
import com.example.scan_dineCustomer.table.service.OrderManagementService;
import com.example.scan_dineCustomer.table.service.ReservationService;
import com.example.scan_dineCustomer.table.service.TableManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class CustomerTableController {

    private final OrderManagementService orderService;
    private final TableManagementService tableService;
    private final ReservationService reservationService;

    @PostMapping("/scan")
    public ResponseEntity<?> scanTable(@RequestBody ScanTableRequest request) {
        try {
            if (request == null) return badRequest("Request body is required");
            if (!StringUtils.hasText(request.getRestaurantId())) return badRequest("restaurantId is required");
            if (!StringUtils.hasText(request.getTableId())) return badRequest("tableId is required");
            return ResponseEntity.ok(orderService.scanTable(request));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error scanning table: " + e.getMessage());
        }
    }

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            if (request == null) return badRequest("Request body is required");
            if (!StringUtils.hasText(request.getSessionId())) return badRequest("sessionId is required");
            if (!StringUtils.hasText(request.getRestaurantId())) return badRequest("restaurantId is required");
            if (request.getItems() == null || request.getItems().isEmpty()) return badRequest("items are required");
            return ResponseEntity.ok(orderService.placeOrder(request));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing order: " + e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}/orders")
    public ResponseEntity<?> getSessionOrders(@PathVariable String sessionId) {
        try {
            if (!StringUtils.hasText(sessionId)) return badRequest("sessionId is required");
            return ResponseEntity.ok(orderService.getSessionOrders(sessionId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching session orders: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            if (!StringUtils.hasText(orderId)) return badRequest("orderId is required");
            return ResponseEntity.ok(orderService.getOrder(orderId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching order: " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam String restaurantId) {
        try {
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
            if (!StringUtils.hasText(customerId)) return badRequest("customerId is required");
            return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId, restaurantId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching orders: " + e.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTables(@RequestParam String restaurantId) {
        try {
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(tableService.getAvailableTables(restaurantId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Restaurant not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching available tables: " + e.getMessage());
        }
    }

    @GetMapping("/{tableId}/status")
    public ResponseEntity<?> getTableStatus(@PathVariable String tableId, @RequestParam String restaurantId) {
        try {
            if (!StringUtils.hasText(tableId)) return badRequest("tableId is required");
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(tableService.getTableStatus(tableId, restaurantId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Table not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching table status: " + e.getMessage());
        }
    }

    // ─── Feature 2: Bill Request & Call Waiter ───────────────────────────────────

    @PostMapping("/session/{sessionId}/bill-request")
    public ResponseEntity<?> requestBill(@PathVariable String sessionId, @RequestParam String restaurantId) {
        try {
            if (!StringUtils.hasText(sessionId)) return badRequest("sessionId is required");
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(orderService.requestBill(sessionId, restaurantId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error requesting bill: " + e.getMessage());
        }
    }

    @PostMapping("/session/{sessionId}/call-waiter")
    public ResponseEntity<?> callWaiter(@PathVariable String sessionId, @RequestParam String restaurantId) {
        try {
            if (!StringUtils.hasText(sessionId)) return badRequest("sessionId is required");
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(orderService.callWaiter(sessionId, restaurantId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error calling waiter: " + e.getMessage());
        }
    }

    // ─── Feature 3: Order Rating ─────────────────────────────────────────────────

    @PostMapping("/orders/{orderId}/rate")
    public ResponseEntity<?> rateOrder(@PathVariable String orderId, @RequestBody OrderRatingRequest request) {
        try {
            if (!StringUtils.hasText(orderId)) return badRequest("orderId is required");
            if (request == null) return badRequest("Request body is required");
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
            if (!StringUtils.hasText(customerId)) return badRequest("customerId is required");
            return ResponseEntity.ok(orderService.rateOrder(orderId, request, customerId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error rating order: " + e.getMessage());
        }
    }

    // ─── Feature 4: Estimated Wait Time ─────────────────────────────────────────

    @GetMapping("/orders/{orderId}/wait-time")
    public ResponseEntity<?> getWaitTime(@PathVariable String orderId) {
        try {
            if (!StringUtils.hasText(orderId)) return badRequest("orderId is required");
            return ResponseEntity.ok(orderService.getEstimatedWaitTime(orderId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching wait time: " + e.getMessage());
        }
    }

    // ─── Feature 6: Reorder ──────────────────────────────────────────────────────

    @PostMapping("/reorder")
    public ResponseEntity<?> reorder(@RequestBody ReorderRequest request) {
        try {
            if (request == null) return badRequest("Request body is required");
            if (!StringUtils.hasText(request.getPreviousOrderId())) return badRequest("previousOrderId is required");
            if (!StringUtils.hasText(request.getSessionId())) return badRequest("sessionId is required");
            if (!StringUtils.hasText(request.getRestaurantId())) return badRequest("restaurantId is required");
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
            if (!StringUtils.hasText(customerId)) return badRequest("customerId is required");
            return ResponseEntity.ok(orderService.reorder(
                    request.getPreviousOrderId(), request.getSessionId(), request.getRestaurantId(), customerId));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing reorder: " + e.getMessage());
        }
    }

    // ─── Feature 9: Reservations ─────────────────────────────────────────────────

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            if (request == null) return badRequest("Request body is required");
            if (!StringUtils.hasText(request.getRestaurantId())) return badRequest("restaurantId is required");
            if (!StringUtils.hasText(request.getCustomerName())) return badRequest("customerName is required");
            if (!StringUtils.hasText(request.getCustomerMobile())) return badRequest("customerMobile is required");
            if (request.getReservedFor() == null) return badRequest("reservedFor is required");
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating reservation: " + e.getMessage());
        }
    }

    @GetMapping("/reservations/lookup")
    public ResponseEntity<?> getMyReservations(@RequestParam String mobile) {
        try {
            if (!StringUtils.hasText(mobile)) return badRequest("mobile is required");
            return ResponseEntity.ok(reservationService.getByMobile(mobile));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No reservations found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching reservations: " + e.getMessage());
        }
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
