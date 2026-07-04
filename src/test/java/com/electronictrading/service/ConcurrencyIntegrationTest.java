package com.electronictrading.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.electronictrading.model.Order;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import com.electronictrading.model.OrderType;
import com.electronictrading.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.electronictrading.ElectronicTradingApplication;

@SpringBootTest(classes = ElectronicTradingApplication.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConcurrencyIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldThrowOptimisticLockingExceptionOnConcurrentUpdates() {
        Order savedOrder = orderRepository.saveAndFlush(Order.builder()
                .userId("user-1")
                .instrument("AAPL")
                .side(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.NEW)
                .timestamp(Instant.now())
                .build());

        UUID orderId = savedOrder.getOrderId();

        Order order1 = orderRepository.findById(orderId).orElseThrow();
        Order order2 = orderRepository.findById(orderId).orElseThrow();

        order1.setQuantity(new BigDecimal("8"));
        orderRepository.saveAndFlush(order1);

        order2.setQuantity(new BigDecimal("6"));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> orderRepository.saveAndFlush(order2));
    }
}