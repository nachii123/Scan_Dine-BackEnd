package com.example.scan_dineCustomer.table.service;

import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.enums.SessionStatus;
import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.table.dto.CaptainTableView;
import com.example.scan_dineCustomer.table.dto.CreateTableRequest;
import com.example.scan_dineCustomer.table.dto.SessionResponse;
import com.example.scan_dineCustomer.table.dto.TableResponse;
import com.example.scan_dineCustomer.table.dto.TableStatusResponse;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import com.example.scan_dineCustomer.table.entity.TableSession;
import com.example.scan_dineCustomer.table.repository.OrderRepository;
import com.example.scan_dineCustomer.table.repository.RestaurantTableRepository;
import com.example.scan_dineCustomer.table.repository.TableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableManagementService {

    private final RestaurantTableRepository tableRepository;
    private final TableSessionRepository sessionRepository;
    private final OrderRepository orderRepository;

    @Transactional("tableTransactionManager")
    public TableResponse createTable(CreateTableRequest request) {
        if (tableRepository.existsByRestaurantIdAndTableNumber(request.getRestaurantId(), request.getTableNumber())) {
            throw new IllegalArgumentException("Table number '" + request.getTableNumber() + "' already exists for this restaurant");
        }
        RestaurantTable table = new RestaurantTable();
        table.setRestaurantId(request.getRestaurantId());
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setFloor(request.getFloor());
        return TableResponse.from(tableRepository.save(table));
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<TableResponse> getTablesByRestaurant(String restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId).stream()
                .map(TableResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<CaptainTableView> getCaptainView(String restaurantId) {
        List<RestaurantTable> tables = tableRepository.findByRestaurantId(restaurantId);
        return tables.stream().map(t -> {
            String activeSessionId = null;
            long pendingCount = 0;
            boolean billRequested = false;
            Instant billRequestedAt = null;
            boolean waiterCalled = false;
            Instant waiterCalledAt = null;
            if (t.getStatus() != TableStatus.AVAILABLE && t.getStatus() != TableStatus.INACTIVE) {
                Optional<TableSession> session = sessionRepository.findByTable_IdAndStatus(t.getId(), SessionStatus.ACTIVE);
                if (session.isPresent()) {
                    TableSession s = session.get();
                    activeSessionId = s.getId();
                    pendingCount = orderRepository.countBySessionIdAndStatus(activeSessionId, OrderStatus.PENDING);
                    billRequested = s.isBillRequested();
                    billRequestedAt = s.getBillRequestedAt();
                    waiterCalled = s.isWaiterCalled();
                    waiterCalledAt = s.getWaiterCalledAt();
                }
            }
            CaptainTableView view = new CaptainTableView();
            view.setId(t.getId());
            view.setTableNumber(t.getTableNumber());
            view.setCapacity(t.getCapacity());
            view.setStatus(t.getStatus());
            view.setFloor(t.getFloor());
            view.setQrCode(t.getQrCode());
            view.setActive(t.isActive());
            view.setActiveSessionId(activeSessionId);
            view.setPendingOrderCount(pendingCount);
            view.setBillRequested(billRequested);
            view.setBillRequestedAt(billRequestedAt);
            view.setWaiterCalled(waiterCalled);
            view.setWaiterCalledAt(waiterCalledAt);
            return view;
        }).collect(Collectors.toList());
    }

    @Transactional("tableTransactionManager")
    public void clearTable(String tableId, String restaurantId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        sessionRepository.findByTable_IdAndStatus(tableId, SessionStatus.ACTIVE).ifPresent(session -> {
            session.setStatus(SessionStatus.CLOSED);
            session.setEndTime(Instant.now());
            sessionRepository.save(session);
        });

        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<TableResponse> getAvailableTables(String restaurantId) {
        return tableRepository.findByRestaurantId(restaurantId).stream()
                .filter(t -> t.isActive() && t.getStatus() == TableStatus.AVAILABLE)
                .map(TableResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public TableResponse getTableStatus(String tableId, String restaurantId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        return TableResponse.from(table);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public TableStatusResponse getTableStatusSafe(String tableId, String restaurantId, String customerId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        boolean canPlaceOrder = false;
        var session = sessionRepository.findByTable_IdAndStatus(tableId, SessionStatus.ACTIVE).orElse(null);
        if (session != null && customerId != null && customerId.equals(session.getCustomerId())) {
            canPlaceOrder = true;
        }
        if (table.getStatus() == TableStatus.AVAILABLE && table.isActive()) {
            canPlaceOrder = true;
        }
        return TableStatusResponse.from(table, canPlaceOrder);
    }

    @Transactional("tableTransactionManager")
    public SessionResponse createSessionForTable(String tableId, String restaurantId, int customerCount) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (!table.isActive()) {
            throw new IllegalStateException("This table is currently inactive");
        }

        Optional<TableSession> existing = sessionRepository.findByTable_IdAndStatus(table.getId(), SessionStatus.ACTIVE);
        if (existing.isPresent()) {
            return SessionResponse.tableOccupied(table);
        }

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table is not available. Current status: " + table.getStatus());
        }

        TableSession session = new TableSession();
        session.setTable(table);
        session.setRestaurantId(restaurantId);
        session.setCustomerCount(customerCount > 0 ? customerCount : 1);
        session.setStatus(SessionStatus.ACTIVE);

        TableSession saved = sessionRepository.save(session);
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        return SessionResponse.scanSuccess(saved);
    }

    @Transactional("tableTransactionManager")
    public TableResponse setTableActive(String tableId, String restaurantId, boolean active) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        table.setActive(active);
        if (!active) {
            table.setStatus(TableStatus.INACTIVE);
        } else if (table.getStatus() == TableStatus.INACTIVE) {
            table.setStatus(TableStatus.AVAILABLE);
        }
        return TableResponse.from(tableRepository.save(table));
    }
}
