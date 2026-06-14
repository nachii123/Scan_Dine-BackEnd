package com.example.scan_dineCustomer.table.service;

import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.entity.Restaurant;
import com.example.scan_dineCustomer.enums.*;
import com.example.scan_dineCustomer.repo.CustomerRepository;
import com.example.scan_dineCustomer.repo.MenuItemRepository;
import com.example.scan_dineCustomer.repo.RestaurantRepository;
import com.example.scan_dineCustomer.table.dto.BillResponse;
import com.example.scan_dineCustomer.table.dto.DailySalesReport;
import com.example.scan_dineCustomer.table.dto.EstimatedWaitResponse;
import com.example.scan_dineCustomer.table.dto.KitchenOrderView;
import com.example.scan_dineCustomer.table.dto.KotResponse;
import com.example.scan_dineCustomer.table.dto.OrderItemRequest;
import com.example.scan_dineCustomer.table.dto.OrderRatingRequest;
import com.example.scan_dineCustomer.table.dto.OrderRatingResponse;
import com.example.scan_dineCustomer.table.dto.OrderResponse;
import com.example.scan_dineCustomer.table.dto.GuestOrderRequest;
import com.example.scan_dineCustomer.table.dto.PlaceOrderRequest;
import com.example.scan_dineCustomer.table.dto.ScanTableRequest;
import com.example.scan_dineCustomer.table.dto.SessionResponse;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import com.example.scan_dineCustomer.table.entity.OrderItem;
import com.example.scan_dineCustomer.table.entity.OrderRating;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import com.example.scan_dineCustomer.table.entity.TableSession;
import com.example.scan_dineCustomer.table.repository.OrderRatingRepository;
import com.example.scan_dineCustomer.table.repository.OrderRepository;
import com.example.scan_dineCustomer.table.repository.RestaurantTableRepository;
import com.example.scan_dineCustomer.table.repository.TableSessionRepository;
import com.example.scan_dineCustomer.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final RestaurantTableRepository tableRepository;
    private final TableSessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final OrderRatingRepository orderRatingRepository;

    @Transactional("tableTransactionManager")
    public SessionResponse scanTable(ScanTableRequest request) {
        String customerId = extractCustomerIdFromToken();
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(request.getTableId(), request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (!table.isActive()) {
            throw new IllegalStateException("This table is currently inactive");
        }

        // Do not expose another customer's active session when the same QR is scanned again.
        Optional<TableSession> existing = sessionRepository.findByTable_IdAndStatus(table.getId(), SessionStatus.ACTIVE);
        if (existing.isPresent()) {
            TableSession activeSession = existing.get();
            assertSessionOwnership(activeSession, customerId, request.getRestaurantId(), request.getTableId());
            return SessionResponse.from(activeSession);
        }

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table is not available. Current status: " + table.getStatus());
        }

        TableSession session = new TableSession();
        session.setTable(table);
        session.setRestaurantId(request.getRestaurantId());
        session.setCustomerId(customerId);
        session.setCustomerCount(request.getCustomerCount() > 0 ? request.getCustomerCount() : 1);
        session.setStatus(SessionStatus.ACTIVE);

        TableSession saved = sessionRepository.save(session);

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        return SessionResponse.scanSuccess(saved);
    }

    @Transactional("tableTransactionManager")
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        String customerId = extractCustomerIdFromToken();
        TableSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + request.getSessionId()));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        assertSessionOwnership(session, customerId, request.getRestaurantId(), null);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }

        Customer customer = customerRepository.findById(customerId).orElse(null);

        DineOrder order = new DineOrder();
        order.setSession(session);
        order.setRestaurantId(request.getRestaurantId());
        order.setTableId(session.getTable().getId());
        order.setTableNumber(session.getTable().getTableNumber());
        order.setCustomerId(customerId);
        if (customer != null) {
            order.setCustomerName(customer.getName());
            order.setCustomerMobile(customer.getMobile());
        }
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemReq.getMenuItemId()));
            log.info("Menu {}", menuItem);

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item is currently unavailable: " + menuItem.getName());
            }

            BigDecimal lineTotal = menuItem.getBasePrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setImageUrl(menuItem.getImageUrl());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(menuItem.getBasePrice());
            item.setTotalPrice(lineTotal);
            item.setNotes(itemReq.getNotes());
            item.setStatus(OrderItemStatus.PENDING);

            order.getItems().add(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        DineOrder saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(String customerId, String restaurantId) {
        return orderRepository.findByCustomerIdAndRestaurantIdOrderByCreatedAtDesc(customerId, restaurantId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public OrderResponse getOrderForCustomer(String orderId, String customerId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!customerId.equals(order.getCustomerId())) {
            throw new AccessDeniedException("You do not have access to this order");
        }
        return OrderResponse.from(order);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderResponse> getSessionOrders(String sessionId) {
        return orderRepository.findBySessionId(sessionId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderResponse> getSessionOrdersForCustomer(String sessionId, String customerId) {
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        assertSessionOwnership(session, customerId, session.getRestaurantId(), null);
        return orderRepository.findBySessionId(sessionId).stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public OrderResponse getOrder(String orderId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderResponse> getPendingOrders(String restaurantId) {
        List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_ACCEPTED);
        return orderRepository.findByRestaurantIdAndStatusIn(restaurantId, statuses).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderResponse> getActiveOrders(String restaurantId) {
        List<OrderStatus> statuses = List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY);
        return orderRepository.findByRestaurantIdAndStatusIn(restaurantId, statuses).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional("tableTransactionManager")
    public OrderResponse acceptOrder(String orderId, String captainId, String captainName) {
        log.info("Accepting order {}", orderId);
        DineOrder order = orderRepository.findDineOrderById(orderId);
        if(ObjectUtils.isEmpty(order)){
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);
        order.getItems().stream()
                .filter(i -> i.getStatus() == OrderItemStatus.PENDING)
                .forEach(i -> i.setStatus(OrderItemStatus.ACCEPTED));

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional("tableTransactionManager")
    public OrderResponse markOrderPreparing(String orderId, String captainId, String captainName) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.ACCEPTED && order.getStatus() != OrderStatus.PARTIALLY_ACCEPTED) {
            throw new IllegalStateException("Order must be ACCEPTED before marking as PREPARING");
        }
        order.setStatus(OrderStatus.PREPARING);
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);
        order.setPreparingStartedAt(Instant.now());
        order.getItems().stream()
                .filter(i -> i.getStatus() == OrderItemStatus.ACCEPTED)
                .forEach(i -> i.setStatus(OrderItemStatus.PREPARING));
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional("tableTransactionManager")
    public OrderResponse markOrderReady(String orderId, String captainId, String captainName) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order must be PREPARING before marking as READY");
        }
        order.setStatus(OrderStatus.READY);
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);
        order.getItems().stream()
                .filter(i -> i.getStatus() == OrderItemStatus.PREPARING)
                .forEach(i -> i.setStatus(OrderItemStatus.READY));
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional("tableTransactionManager")
    public OrderResponse rejectOrder(String orderId, String reason, String captainId, String captainName) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CANCELLED);
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);
        if (reason != null && !reason.isBlank()) {
            order.setNotes(reason);
        }
        order.getItems().stream()
                .filter(i -> i.getStatus() == OrderItemStatus.PENDING)
                .forEach(i -> i.setStatus(OrderItemStatus.REJECTED));

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional("tableTransactionManager")
    public OrderResponse serveItem(String orderId, String itemId, String captainId, String captainName) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in this order"))
                .setStatus(OrderItemStatus.SERVED);

        order.setCaptainId(captainId);
        order.setCaptainName(captainName);

        boolean allDone = order.getItems().stream()
                .allMatch(i -> i.getStatus() == OrderItemStatus.SERVED || i.getStatus() == OrderItemStatus.REJECTED);
        if (allDone) {
            order.setStatus(OrderStatus.SERVED);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    public String extractCustomerIdFromSecurityContext(
            org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }
        String token = auth.getCredentials() != null ? auth.getCredentials().toString() : null;
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authentication token missing");
        }
        return jwtUtil.extractCustomerId(token);
    }

    private String extractCustomerIdFromToken() {
        return extractCustomerIdFromSecurityContext(
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Transactional("tableTransactionManager")
    public OrderResponse captainAddItems(String orderId, List<OrderItemRequest> itemRequests, String captainId, String captainName) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);

        BigDecimal extra = BigDecimal.ZERO;

        for (OrderItemRequest req : itemRequests) {
            MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + req.getMenuItemId()));

            BigDecimal lineTotal = menuItem.getBasePrice().multiply(BigDecimal.valueOf(req.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setImageUrl(menuItem.getImageUrl());
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(menuItem.getBasePrice());
            item.setTotalPrice(lineTotal);
            item.setNotes(req.getNotes());
            // Captain-added items skip the PENDING state
            item.setStatus(OrderItemStatus.ACCEPTED);

            order.getItems().add(item);
            extra = extra.add(lineTotal);
        }

        order.setTotalAmount(order.getTotalAmount().add(extra));

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional("tableTransactionManager")
    public OrderResponse placeGuestOrder(GuestOrderRequest request, String captainId, String captainName) {
        TableSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + request.getSessionId()));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        String guestId = "GUEST_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        DineOrder order = new DineOrder();
        order.setSession(session);
        order.setRestaurantId(request.getRestaurantId());
        order.setTableId(session.getTable().getId());
        order.setTableNumber(session.getTable().getTableNumber());
        order.setCustomerId(guestId);
        order.setCustomerName(request.getGuestName() != null ? request.getGuestName() : "Guest");
        order.setCustomerMobile(request.getGuestMobile());
        order.setCaptainId(captainId);
        order.setCaptainName(captainName);
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item is currently unavailable: " + menuItem.getName());
            }

            BigDecimal lineTotal = menuItem.getBasePrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setImageUrl(menuItem.getImageUrl());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(menuItem.getBasePrice());
            item.setTotalPrice(lineTotal);
            item.setNotes(itemReq.getNotes());
            item.setStatus(OrderItemStatus.PENDING);

            order.getItems().add(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    // ─── Feature 2: Bill Request & Call Waiter ───────────────────────────────────

    @Transactional("tableTransactionManager")
    public SessionResponse requestBill(String sessionId, String restaurantId) {
        TableSession session = sessionRepository.findByIdAndRestaurantId(sessionId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        assertSessionOwnership(session, extractCustomerIdFromToken(), restaurantId, null);
        session.setBillRequested(true);
        session.setBillRequestedAt(Instant.now());
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional("tableTransactionManager")
    public SessionResponse callWaiter(String sessionId, String restaurantId) {
        TableSession session = sessionRepository.findByIdAndRestaurantId(sessionId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        assertSessionOwnership(session, extractCustomerIdFromToken(), restaurantId, null);
        session.setWaiterCalled(true);
        session.setWaiterCalledAt(Instant.now());
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional("tableTransactionManager")
    public SessionResponse acknowledgeBillRequest(String sessionId) {
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setBillRequested(false);
        session.setBillRequestedAt(null);
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional("tableTransactionManager")
    public SessionResponse acknowledgeWaiterCall(String sessionId) {
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setWaiterCalled(false);
        session.setWaiterCalledAt(null);
        return SessionResponse.from(sessionRepository.save(session));
    }

    // ─── Feature 3: Order Rating ─────────────────────────────────────────────────

    @Transactional("tableTransactionManager")
    public OrderRatingResponse rateOrder(String orderId, OrderRatingRequest request, String customerId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new IllegalStateException("You can only rate your own orders");
        }
        if (order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("You can only rate a served order");
        }
        if (orderRatingRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Order has already been rated");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        OrderRating rating = new OrderRating();
        rating.setOrderId(orderId);
        rating.setCustomerId(customerId);
        rating.setRestaurantId(order.getRestaurantId());
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        return OrderRatingResponse.from(orderRatingRepository.save(rating));
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<OrderRatingResponse> getRestaurantRatings(String restaurantId) {
        return orderRatingRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(OrderRatingResponse::from).collect(Collectors.toList());
    }

    // ─── Feature 4: Estimated Wait Time ─────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public EstimatedWaitResponse getEstimatedWaitTime(String orderId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<EstimatedWaitResponse.ItemWaitInfo> itemInfos = new ArrayList<>();
        int totalEstimated = 0;

        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == OrderItemStatus.REJECTED || item.getStatus() == OrderItemStatus.CANCELLED) continue;
            MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId()).orElse(null);
            int prepTime = menuItem != null ? menuItem.getPrepTimeMinutes() : 15;
            totalEstimated = Math.max(totalEstimated, prepTime); // parallel cooking
            EstimatedWaitResponse.ItemWaitInfo itemInfo = new EstimatedWaitResponse.ItemWaitInfo();
            itemInfo.setMenuItemName(item.getMenuItemName());
            itemInfo.setQuantity(item.getQuantity());
            itemInfo.setPrepTimeMinutes(prepTime);
            itemInfos.add(itemInfo);
        }

        long preparingFor = 0;
        if (order.getPreparingStartedAt() != null) {
            preparingFor = Duration.between(order.getPreparingStartedAt(), Instant.now()).toMinutes();
        }
        int remaining = (int) Math.max(0, totalEstimated - preparingFor);

        EstimatedWaitResponse response = new EstimatedWaitResponse();
        response.setOrderId(orderId);
        response.setEstimatedMinutes(totalEstimated);
        response.setPreparingForMinutes(preparingFor);
        response.setRemainingMinutes(remaining);
        response.setItems(itemInfos);
        return response;
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public EstimatedWaitResponse getEstimatedWaitTimeForCustomer(String orderId, String customerId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!customerId.equals(order.getCustomerId())) {
            throw new AccessDeniedException("You do not have access to this order");
        }
        return getEstimatedWaitTime(orderId);
    }

    // ─── Feature 6: Reorder ──────────────────────────────────────────────────────

    @Transactional("tableTransactionManager")
    public OrderResponse reorder(String previousOrderId, String sessionId, String restaurantId, String customerId) {
        DineOrder previousOrder = orderRepository.findById(previousOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Previous order not found: " + previousOrderId));

        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        DineOrder newOrder = new DineOrder();
        newOrder.setSession(session);
        newOrder.setRestaurantId(restaurantId);
        newOrder.setTableId(session.getTable().getId());
        newOrder.setCustomerId(customerId);
        newOrder.setNotes("Reorder from " + previousOrderId);
        newOrder.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem prevItem : previousOrder.getItems()) {
            if (prevItem.getStatus() == OrderItemStatus.REJECTED || prevItem.getStatus() == OrderItemStatus.CANCELLED) continue;
            MenuItem menuItem = menuItemRepository.findById(prevItem.getMenuItemId()).orElse(null);
            if (menuItem == null || !menuItem.isAvailable()) continue;

            BigDecimal lineTotal = menuItem.getBasePrice().multiply(BigDecimal.valueOf(prevItem.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(newOrder);
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setImageUrl(menuItem.getImageUrl());
            item.setQuantity(prevItem.getQuantity());
            item.setUnitPrice(menuItem.getBasePrice());
            item.setTotalPrice(lineTotal);
            item.setNotes(prevItem.getNotes());
            item.setStatus(OrderItemStatus.PENDING);

            newOrder.getItems().add(item);
            total = total.add(lineTotal);
        }

        if (newOrder.getItems().isEmpty()) {
            throw new IllegalStateException("No available items to reorder");
        }

        newOrder.setTotalAmount(total);
        return OrderResponse.from(orderRepository.save(newOrder));
    }

    // ─── Feature 7: Kitchen Display ──────────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<KitchenOrderView> getKitchenDisplay(String restaurantId) {
        List<OrderStatus> activeStatuses = List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY);
        return orderRepository.findByRestaurantIdAndStatusIn(restaurantId, activeStatuses)
                .stream()
                .map(order -> {
                    int estimated = order.getItems().stream()
                            .filter(i -> i.getStatus() != OrderItemStatus.REJECTED && i.getStatus() != OrderItemStatus.CANCELLED)
                            .mapToInt(i -> {
                                MenuItem m = menuItemRepository.findById(i.getMenuItemId()).orElse(null);
                                return m != null ? m.getPrepTimeMinutes() : 15;
                            })
                            .max().orElse(15);
                    return KitchenOrderView.from(order, estimated);
                })
                .collect(Collectors.toList());
    }

    // ─── Feature 8: Daily Sales Report ───────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public DailySalesReport getDailySalesReport(String restaurantId, LocalDate date) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

        List<DineOrder> orders = orderRepository
                .findByRestaurantIdAndCreatedAtBetweenAndStatusNot(restaurantId, start, end, OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orders.stream()
                .map(DineOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItemsSold = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getStatus() != OrderItemStatus.REJECTED && i.getStatus() != OrderItemStatus.CANCELLED)
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // Top items
        Map<String, int[]> quantityMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        Map<String, String> nameMap = new LinkedHashMap<>();
        orders.stream().flatMap(o -> o.getItems().stream())
                .filter(i -> i.getStatus() != OrderItemStatus.REJECTED && i.getStatus() != OrderItemStatus.CANCELLED)
                .forEach(i -> {
                    quantityMap.merge(i.getMenuItemId(), new int[]{i.getQuantity()}, (a, b) -> new int[]{a[0] + b[0]});
                    revenueMap.merge(i.getMenuItemId(), i.getTotalPrice(), BigDecimal::add);
                    nameMap.putIfAbsent(i.getMenuItemId(), i.getMenuItemName());
                });

        List<DailySalesReport.TopItemStats> topItems = quantityMap.entrySet().stream()
                .map(e -> {
                    DailySalesReport.TopItemStats stats = new DailySalesReport.TopItemStats();
                    stats.setMenuItemId(e.getKey());
                    stats.setMenuItemName(nameMap.get(e.getKey()));
                    stats.setQuantitySold(e.getValue()[0]);
                    stats.setRevenue(revenueMap.getOrDefault(e.getKey(), BigDecimal.ZERO));
                    return stats;
                })
                .sorted((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()))
                .limit(10)
                .collect(Collectors.toList());

        // Hourly stats
        Map<Integer, List<DineOrder>> byHour = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().atZone(zone).getHour()));

        List<DailySalesReport.HourlyStats> hourlyStats = byHour.entrySet().stream()
                .map(e -> {
                    DailySalesReport.HourlyStats stats = new DailySalesReport.HourlyStats();
                    stats.setHour(e.getKey());
                    stats.setOrderCount(e.getValue().size());
                    stats.setRevenue(e.getValue().stream().map(DineOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    return stats;
                })
                .sorted(java.util.Comparator.comparingInt(DailySalesReport.HourlyStats::getHour))
                .collect(Collectors.toList());

        DailySalesReport report = new DailySalesReport();
        report.setDate(date);
        report.setRestaurantId(restaurantId);
        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders(orders.size());
        report.setTotalItemsSold(totalItemsSold);
        report.setTopItems(topItems);
        report.setRevenueByHour(hourlyStats);
        return report;
    }

    // ─── KOT (Kitchen Order Ticket) ──────────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public KotResponse generateKot(String orderId) {
        DineOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<KotResponse.KotItem> kotItems = order.getItems().stream()
                .filter(i -> i.getStatus() != OrderItemStatus.REJECTED && i.getStatus() != OrderItemStatus.CANCELLED)
                .map(i -> {
                    KotResponse.KotItem item = new KotResponse.KotItem();
                    item.setMenuItemName(i.getMenuItemName());
                    item.setQuantity(i.getQuantity());
                    item.setNotes(i.getNotes());
                    return item;
                })
                .collect(Collectors.toList());

        KotResponse response = new KotResponse();
        response.setKotNumber(order.getId());
        response.setTableNumber(order.getSession().getTable().getTableNumber());
        response.setRestaurantId(order.getRestaurantId());
        response.setCaptainId(order.getCaptainId());
        response.setCaptainName(order.getCaptainName());
        response.setGeneratedAt(Instant.now());
        response.setItems(kotItems);
        return response;
    }

    // ─── Bill ─────────────────────────────────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public BillResponse generateBill(String sessionId) {
        TableSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        Restaurant restaurant = restaurantRepository.findById(session.getRestaurantId()).orElse(null);

        List<DineOrder> activeOrders = session.getOrders().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        List<BillResponse.BillItem> billItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DineOrder order : activeOrders) {
            for (OrderItem item : order.getItems()) {
                if (item.getStatus() == OrderItemStatus.REJECTED || item.getStatus() == OrderItemStatus.CANCELLED) continue;
                BillResponse.BillItem billItem = new BillResponse.BillItem();
                billItem.setOrderId(order.getId());
                billItem.setMenuItemName(item.getMenuItemName());
                billItem.setQuantity(item.getQuantity());
                billItem.setUnitPrice(item.getUnitPrice());
                billItem.setTotalPrice(item.getTotalPrice());
                billItems.add(billItem);
                subtotal = subtotal.add(item.getTotalPrice());
            }
        }

        // Captain who last acted on any order in this session
        String captainId = null;
        String captainName = null;
        for (DineOrder order : activeOrders) {
            if (order.getCaptainId() != null) {
                captainId = order.getCaptainId();
                captainName = order.getCaptainName();
            }
        }

        BillResponse response = new BillResponse();
        response.setBillNumber(session.getId());
        response.setSessionId(session.getId());
        response.setTableNumber(session.getTable().getTableNumber());
        response.setRestaurantId(session.getRestaurantId());
        response.setRestaurantName(restaurant != null ? restaurant.getName() : null);
        response.setGstin(restaurant != null ? restaurant.getGstin() : null);
        response.setCurrency(restaurant != null ? restaurant.getCurrency() : "INR");
        response.setCustomerCount(session.getCustomerCount());
        response.setCaptainId(captainId);
        response.setCaptainName(captainName);
        response.setSessionStartTime(session.getStartTime());
        response.setGeneratedAt(Instant.now());
        response.setItems(billItems);
        response.setSubtotal(subtotal);
        return response;
    }

    // ─── Feature 10: Peak Hour Analytics ─────────────────────────────────────────

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<DailySalesReport.HourlyStats> getPeakHourAnalytics(String restaurantId, LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant start = from.atStartOfDay(zone).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();

        List<DineOrder> orders = orderRepository
                .findByRestaurantIdAndCreatedAtBetweenAndStatusNot(restaurantId, start, end, OrderStatus.CANCELLED);

        Map<Integer, List<DineOrder>> byHour = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().atZone(zone).getHour()));

        return IntStream.range(0, 24)
                .mapToObj(hour -> {
                    List<DineOrder> hourOrders = byHour.getOrDefault(hour, List.of());
                    BigDecimal revenue = hourOrders.stream()
                            .map(DineOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    DailySalesReport.HourlyStats stats = new DailySalesReport.HourlyStats();
                    stats.setHour(hour);
                    stats.setOrderCount(hourOrders.size());
                    stats.setRevenue(revenue);
                    return stats;
                })
                .collect(Collectors.toList());
    }

    private void assertSessionOwnership(TableSession session, String customerId, String restaurantId, String tableId) {
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        if (!customerId.equals(session.getCustomerId())) {
            throw new AccessDeniedException("Session is owned by another customer");
        }
        if (!restaurantId.equals(session.getRestaurantId())) {
            throw new IllegalStateException("Session does not belong to this restaurant");
        }
        if (tableId != null && session.getTable() != null && !tableId.equals(session.getTable().getId())) {
            throw new IllegalStateException("Session does not belong to this table");
        }
    }
}
