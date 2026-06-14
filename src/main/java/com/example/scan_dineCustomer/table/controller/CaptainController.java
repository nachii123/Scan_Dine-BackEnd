package com.example.scan_dineCustomer.table.controller;

import com.example.scan_dineCustomer.config.CaptainPrincipal;
import com.example.scan_dineCustomer.enums.ReservationStatus;
import com.example.scan_dineCustomer.restaurant.dto.MenuItemResponse;
import com.example.scan_dineCustomer.restaurant.service.MenuService;
import com.example.scan_dineCustomer.table.dto.AcceptRejectRequest;
import com.example.scan_dineCustomer.table.dto.GuestOrderRequest;
import com.example.scan_dineCustomer.table.dto.BillResponse;
import com.example.scan_dineCustomer.table.dto.CaptainTableView;
import com.example.scan_dineCustomer.table.dto.CreateTableRequest;
import com.example.scan_dineCustomer.table.dto.DailySalesReport;
import com.example.scan_dineCustomer.table.dto.KitchenOrderView;
import com.example.scan_dineCustomer.table.dto.KotResponse;
import com.example.scan_dineCustomer.table.dto.OrderItemRequest;
import com.example.scan_dineCustomer.table.dto.OrderRatingResponse;
import com.example.scan_dineCustomer.table.dto.OrderResponse;
import com.example.scan_dineCustomer.table.dto.ReservationRequest;
import com.example.scan_dineCustomer.table.dto.ReservationResponse;
import com.example.scan_dineCustomer.table.dto.SessionResponse;
import com.example.scan_dineCustomer.table.dto.TableResponse;
import com.example.scan_dineCustomer.table.service.OrderManagementService;
import com.example.scan_dineCustomer.table.service.ReservationService;
import com.example.scan_dineCustomer.table.service.TableManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/captain")
@RequiredArgsConstructor
public class CaptainController {

    private final TableManagementService tableService;
    private final OrderManagementService orderService;
    private final MenuService menuService;
    private final ReservationService reservationService;

    // ─── Table Management ───────────────────────────────────────────────────────

    @PostMapping("/tables")
    public ResponseEntity<TableResponse> createTable(@RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(tableService.createTable(request));
    }

    @GetMapping("/{restaurantId}/tables")
    public ResponseEntity<List<CaptainTableView>> getCaptainView(@PathVariable String restaurantId) {
        return ResponseEntity.ok(tableService.getCaptainView(restaurantId));
    }

    @PostMapping("/tables/{tableId}/clear")
    public ResponseEntity<Map<String, String>> clearTable(
            @PathVariable String tableId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        tableService.clearTable(tableId, captain.getRestaurantId());
        return ResponseEntity.ok(Map.of("message", "Table cleared successfully"));
    }

    @PostMapping("/tables/{tableId}/session")
    public ResponseEntity<?> createTableSession(
            @PathVariable String tableId,
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam(defaultValue = "1") int customerCount) {
        try {
            SessionResponse response = tableService.createSessionForTable(tableId, captain.getRestaurantId(), customerCount);
            if (!response.isCanPlaceOrder()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating session: " + e.getMessage());
        }
    }

    @PutMapping("/tables/{tableId}/active")
    public ResponseEntity<TableResponse> setTableActive(
            @PathVariable String tableId,
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam boolean active) {
        return ResponseEntity.ok(tableService.setTableActive(tableId, captain.getRestaurantId(), active));
    }

    // ─── Order Management ────────────────────────────────────────────────────────

    @GetMapping("/orders/pending/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getPendingOrders(
            @PathVariable String restaurantId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        if (!captain.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getPendingOrders(restaurantId));
    }

    @GetMapping("/orders/active/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@PathVariable String restaurantId) {
        return ResponseEntity.ok(orderService.getActiveOrders(restaurantId));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PutMapping("/orders/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.acceptOrder(orderId, captain.getCaptainId(), captain.getName()));
    }

    @PutMapping("/orders/{orderId}/preparing")
    public ResponseEntity<OrderResponse> markPreparing(
            @PathVariable String orderId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.markOrderPreparing(orderId, captain.getCaptainId(), captain.getName()));
    }

    @PutMapping("/orders/{orderId}/ready")
    public ResponseEntity<OrderResponse> markReady(
            @PathVariable String orderId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.markOrderReady(orderId, captain.getCaptainId(), captain.getName()));
    }

    @PutMapping("/orders/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestBody(required = false) AcceptRejectRequest body) {
        String reason = body != null ? body.getReason() : null;
        return ResponseEntity.ok(orderService.rejectOrder(orderId, reason, captain.getCaptainId(), captain.getName()));
    }

    @PutMapping("/orders/{orderId}/items/{itemId}/serve")
    public ResponseEntity<OrderResponse> serveItem(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.serveItem(orderId, itemId, captain.getCaptainId(), captain.getName()));
    }

    @PostMapping("/orders/{orderId}/items")
    public ResponseEntity<OrderResponse> addItemsToOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestBody List<OrderItemRequest> items) {
        return ResponseEntity.ok(orderService.captainAddItems(orderId, items, captain.getCaptainId(), captain.getName()));
    }

    @PostMapping("/orders/guest")
    public ResponseEntity<?> placeGuestOrder(
            @RequestBody GuestOrderRequest request,
            @AuthenticationPrincipal CaptainPrincipal captain) {
        try {
            if (request == null) return ResponseEntity.badRequest().body("Request body is required");
            if (!org.springframework.util.StringUtils.hasText(request.getSessionId())) return ResponseEntity.badRequest().body("sessionId is required");
            if (!org.springframework.util.StringUtils.hasText(request.getRestaurantId())) return ResponseEntity.badRequest().body("restaurantId is required");
            if (request.getItems() == null || request.getItems().isEmpty()) return ResponseEntity.badRequest().body("items are required");
            return ResponseEntity.ok(orderService.placeGuestOrder(request, captain.getCaptainId(), captain.getName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing guest order: " + e.getMessage());
        }
    }

    // ─── KOT & Bill ──────────────────────────────────────────────────────────────

    @GetMapping("/orders/{orderId}/kot")
    public ResponseEntity<KotResponse> getKot(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.generateKot(orderId));
    }

    @GetMapping("/sessions/{sessionId}/orders")
    public ResponseEntity<List<OrderResponse>> getSessionOrders(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.getSessionOrders(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/bill")
    public ResponseEntity<BillResponse> getBill(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.generateBill(sessionId));
    }

    // ─── Feature 2: Bill & Waiter Acknowledgement ────────────────────────────────

    @PutMapping("/sessions/{sessionId}/acknowledge-bill")
    public ResponseEntity<SessionResponse> acknowledgeBill(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.acknowledgeBillRequest(sessionId));
    }

    @PutMapping("/sessions/{sessionId}/acknowledge-waiter")
    public ResponseEntity<SessionResponse> acknowledgeWaiter(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.acknowledgeWaiterCall(sessionId));
    }

    // ─── Feature 3: Ratings ──────────────────────────────────────────────────────

    @GetMapping("/ratings")
    public ResponseEntity<List<OrderRatingResponse>> getRestaurantRatings(@AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.getRestaurantRatings(captain.getRestaurantId()));
    }

    // ─── Feature 5: Item Availability Toggle ─────────────────────────────────────

    @PutMapping("/menu/items/{itemId}/availability")
    public ResponseEntity<MenuItemResponse> toggleItemAvailability(
            @PathVariable String itemId,
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam boolean available) {
        return ResponseEntity.ok(MenuItemResponse.from(menuService.toggleItemAvailability(captain.getRestaurantId(), itemId, available)));
    }

    // ─── Feature 7: Kitchen Display ──────────────────────────────────────────────

    @GetMapping("/kitchen")
    public ResponseEntity<List<KitchenOrderView>> getKitchenDisplay(@AuthenticationPrincipal CaptainPrincipal captain) {
        return ResponseEntity.ok(orderService.getKitchenDisplay(captain.getRestaurantId()));
    }

    // ─── Feature 8: Daily Sales Report ───────────────────────────────────────────

    @GetMapping("/reports/daily")
    public ResponseEntity<DailySalesReport> getDailyReport(
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getDailySalesReport(captain.getRestaurantId(), date));
    }

    // ─── Feature 9: Reservations ─────────────────────────────────────────────────

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getReservationsForDay(captain.getRestaurantId(), date));
    }

    @PutMapping("/reservations/{reservationId}/status")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable String reservationId,
            @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateStatus(reservationId, status));
    }

    // ─── Feature 10: Peak Hour Analytics ─────────────────────────────────────────

    @GetMapping("/reports/peak-hours")
    public ResponseEntity<List<DailySalesReport.HourlyStats>> getPeakHours(
            @AuthenticationPrincipal CaptainPrincipal captain,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(orderService.getPeakHourAnalytics(captain.getRestaurantId(), from, to));
    }
}
