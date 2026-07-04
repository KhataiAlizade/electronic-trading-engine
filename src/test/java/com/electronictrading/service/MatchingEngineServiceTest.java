package com.electronictrading.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.electronictrading.model.Order;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import com.electronictrading.model.OrderType;
import com.electronictrading.model.Trade;
import com.electronictrading.repository.OrderRepository;
import com.electronictrading.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchingEngineServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private MatchingEngineService matchingEngineService;

    @Test
    void shouldMatchLimitOrderPerfectly() {
        Order existingSellOrder = Order.builder()
                .orderId(UUID.randomUUID())
                .instrument("AAPL")
                .side(OrderSide.SELL)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("50"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.NEW)
                .timestamp(Instant.parse("2026-07-04T10:00:00Z"))
                .build();

        Order newBuyOrder = Order.builder()
                .orderId(UUID.randomUUID())
                .instrument("AAPL")
                .side(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("50"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.NEW)
                .timestamp(Instant.parse("2026-07-04T10:01:00Z"))
                .build();

        when(orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceAscTimestampAsc(
                "AAPL", OrderSide.SELL, List.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED)))
                .thenReturn(List.of(existingSellOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchingEngineService.matchOrder(newBuyOrder);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(tradeCaptor.capture());
        assertEquals(new BigDecimal("100"), tradeCaptor.getValue().getPrice());
        assertEquals(new BigDecimal("50"), tradeCaptor.getValue().getQuantity());

        assertEquals(OrderStatus.FILLED, newBuyOrder.getStatus());
        assertEquals(OrderStatus.FILLED, existingSellOrder.getStatus());
    }

    @Test
    void shouldMatchMarketOrder() {
        Order existingBuyOrder = Order.builder()
                .orderId(UUID.randomUUID())
                .instrument("AAPL")
                .side(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(new BigDecimal("25"))
                .price(new BigDecimal("101"))
                .status(OrderStatus.NEW)
                .timestamp(Instant.parse("2026-07-04T10:00:00Z"))
                .build();

        Order newSellOrder = Order.builder()
                .orderId(UUID.randomUUID())
                .instrument("AAPL")
                .side(OrderSide.SELL)
                .orderType(OrderType.MARKET)
                .quantity(new BigDecimal("10"))
                .price(null)
                .status(OrderStatus.NEW)
                .timestamp(Instant.parse("2026-07-04T10:01:00Z"))
                .build();

        when(orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceDescTimestampAsc(
                "AAPL", OrderSide.BUY, List.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED)))
                .thenReturn(List.of(existingBuyOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchingEngineService.matchOrder(newSellOrder);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(tradeCaptor.capture());
        assertEquals(new BigDecimal("101"), tradeCaptor.getValue().getPrice());
        assertEquals(new BigDecimal("10"), tradeCaptor.getValue().getQuantity());

        assertEquals(OrderStatus.FILLED, newSellOrder.getStatus());
        assertEquals(OrderStatus.PARTIALLY_FILLED, existingBuyOrder.getStatus());
    }
}