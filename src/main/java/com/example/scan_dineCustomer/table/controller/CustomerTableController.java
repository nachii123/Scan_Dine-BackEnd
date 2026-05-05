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
    public ResponseEntity<SessionResponse> scanTable(@RequestBody ScanTableRequest request) {
        return ResponseEntity.ok(orderService.scanTable(request));
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/session/{sessionId}/orders")
    public ResponseEntity<List<OrderResponse>> getSessionOrders(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.getSessionOrders(sessionId));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/{tableId}/status")
    public ResponseEntity<TableResponse> getTableStatus(
            @PathVariable String tableId,
            @RequestParam String restaurantId) {
        return ResponseEntity.ok(tableService.getTableStatus(tableId, restaurantId));
    }

    // ─── Feature 2: Bill Request & Call Waiter ───────────────────────────────────

    @PostMapping("/session/{sessionId}/bill-request")
    public ResponseEntity<SessionResponse> requestBill(
            @PathVariable String sessionId,
            @RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.requestBill(sessionId, restaurantId));
    }

    @PostMapping("/session/{sessionId}/call-waiter")
    public ResponseEntity<SessionResponse> callWaiter(
            @PathVariable String sessionId,
            @RequestParam String restaurantId) {
        return ResponseEntity.ok(orderService.callWaiter(sessionId, restaurantId));
    }

    // ─── Feature 3: Order Rating ─────────────────────────────────────────────────

    @PostMapping("/orders/{orderId}/rate")
    public ResponseEntity<OrderRatingResponse> rateOrder(
            @PathVariable String orderId,
            @RequestBody OrderRatingRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
        return ResponseEntity.ok(orderService.rateOrder(orderId, request, customerId));
    }

    // ─── Feature 4: Estimated Wait Time ─────────────────────────────────────────

    @GetMapping("/orders/{orderId}/wait-time")
    public ResponseEntity<EstimatedWaitResponse> getWaitTime(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getEstimatedWaitTime(orderId));
    }

    // ─── Feature 6: Reorder ──────────────────────────────────────────────────────

    @PostMapping("/reorder")
    public ResponseEntity<OrderResponse> reorder(@RequestBody ReorderRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String customerId = orderService.extractCustomerIdFromSecurityContext(auth);
        return ResponseEntity.ok(orderService.reorder(
                request.getPreviousOrderId(), request.getSessionId(), request.getRestaurantId(), customerId));
    }

    // ─── Feature 9: Reservations ─────────────────────────────────────────────────

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    @GetMapping("/reservations/lookup")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@RequestParam String mobile) {
        return ResponseEntity.ok(reservationService.getByMobile(mobile));
    }
}
