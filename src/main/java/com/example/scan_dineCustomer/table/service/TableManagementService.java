package com.example.scan_dineCustomer.table.service;

import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.enums.SessionStatus;
import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.table.dto.CaptainTableView;
import com.example.scan_dineCustomer.table.dto.CreateTableRequest;
import com.example.scan_dineCustomer.table.dto.TableResponse;
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
            if (t.getStatus() != TableStatus.AVAILABLE && t.getStatus() != TableStatus.INACTIVE) {
                Optional<TableSession> session = sessionRepository.findByTable_IdAndStatus(t.getId(), SessionStatus.ACTIVE);
                if (session.isPresent()) {
                    activeSessionId = session.get().getId();
                    pendingCount = orderRepository.countBySessionIdAndStatus(activeSessionId, OrderStatus.PENDING);
                }
            }
            return CaptainTableView.builder()
                    .id(t.getId())
                    .tableNumber(t.getTableNumber())
                    .capacity(t.getCapacity())
                    .status(t.getStatus())
                    .floor(t.getFloor())
                    .qrCode(t.getQrCode())
                    .active(t.isActive())
                    .activeSessionId(activeSessionId)
                    .pendingOrderCount(pendingCount)
                    .build();
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
    public TableResponse getTableStatus(String tableId, String restaurantId) {
        RestaurantTable table = tableRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        return TableResponse.from(table);
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
