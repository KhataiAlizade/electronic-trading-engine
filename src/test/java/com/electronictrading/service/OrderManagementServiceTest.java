package com.electronictrading.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.electronictrading.dto.OrderRequestDTO;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderType;
import com.electronictrading.repository.OrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MatchingEngineService matchingEngineService;

    private OrderManagementService orderManagementService;

    @BeforeEach
    void setUp() {
        orderManagementService = new OrderManagementService(orderRepository, matchingEngineService);
    }

    @Test
    void shouldThrowExceptionWhenLimitOrderHasNoPrice() {
        OrderRequestDTO request = new OrderRequestDTO(
                "AAPL",
                OrderSide.BUY,
                new BigDecimal("10"),
                null,
                OrderType.LIMIT);

        assertThrows(IllegalArgumentException.class, () -> orderManagementService.placeOrder(request));
    }

    @Test
    void shouldThrowExceptionWhenMarketOrderHasPrice() {
        OrderRequestDTO request = new OrderRequestDTO(
                "AAPL",
                OrderSide.SELL,
                new BigDecimal("10"),
                new BigDecimal("100"),
                OrderType.MARKET);

        assertThrows(IllegalArgumentException.class, () -> orderManagementService.placeOrder(request));
    }
}