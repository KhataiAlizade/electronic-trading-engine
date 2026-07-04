package com.electronictrading.service;

import com.electronictrading.dto.OrderRequestDTO;
import com.electronictrading.dto.OrderResponseDTO;
import com.electronictrading.model.Order;
import com.electronictrading.model.OrderStatus;
import com.electronictrading.model.OrderType;
import com.electronictrading.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderManagementService {

    private static final String DEFAULT_USER_ID = "anonymous";

    private final OrderRepository orderRepository;
    private final MatchingEngineService matchingEngineService;

    public OrderManagementService(OrderRepository orderRepository, MatchingEngineService matchingEngineService) {
        this.orderRepository = orderRepository;
        this.matchingEngineService = matchingEngineService;
    }

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        validateOrder(request);

        Order order = toEntity(request);

        order.setStatus(OrderStatus.NEW);
        order.setTimestamp(Instant.now());

        Order savedOrder = orderRepository.save(order);
        matchingEngineService.matchOrder(savedOrder);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(UUID id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(UUID id) {
        Order entity = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        entity.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateOrder(OrderRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Order must not be null");
        }

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (request.type() == null) {
            throw new IllegalArgumentException("Order type must be provided");
        }

        if (request.type() == OrderType.LIMIT) {
            if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Limit order price must be greater than zero");
            }
            return;
        }

        if (request.type() == OrderType.MARKET && request.price() != null) {
            throw new IllegalArgumentException("Market order price must be null");
        }
    }

    private Order toEntity(OrderRequestDTO request) {
        return Order.builder()
                .userId(DEFAULT_USER_ID)
                .instrument(request.instrument())
                .side(request.side())
                .quantity(request.quantity())
                .price(request.price())
                .orderType(request.type())
                .build();
    }

    private OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getOrderId(),
                order.getInstrument(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getTimestamp());
    }
}