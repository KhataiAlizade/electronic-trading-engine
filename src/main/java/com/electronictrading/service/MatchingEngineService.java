package com.electronictrading.service;

import com.electronictrading.model.Order;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import com.electronictrading.model.OrderType;
import com.electronictrading.model.Trade;
import com.electronictrading.repository.OrderRepository;
import com.electronictrading.repository.TradeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingEngineService {

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.NEW,
            OrderStatus.PARTIALLY_FILLED);

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public MatchingEngineService(OrderRepository orderRepository, TradeRepository tradeRepository) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public void matchOrder(Order newOrder) {
        List<Order> opposingOrders = getOpposingOrders(newOrder);

        for (Order opposingOrder : opposingOrders) {
            if (newOrder.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            if (!isMatch(newOrder, opposingOrder)) {
                continue;
            }

            BigDecimal tradedQuantity = newOrder.getQuantity().min(opposingOrder.getQuantity());
            BigDecimal executionPrice = opposingOrder.getPrice();

            newOrder.setQuantity(newOrder.getQuantity().subtract(tradedQuantity));
            opposingOrder.setQuantity(opposingOrder.getQuantity().subtract(tradedQuantity));

            newOrder.setStatus(determineStatus(newOrder.getQuantity()));
            opposingOrder.setStatus(determineStatus(opposingOrder.getQuantity()));

            tradeRepository.save(Trade.builder()
                    .buyOrderId(resolveBuyOrderId(newOrder, opposingOrder))
                    .sellOrderId(resolveSellOrderId(newOrder, opposingOrder))
                    .price(executionPrice)
                    .quantity(tradedQuantity)
                    .timestamp(newOrder.getTimestamp())
                    .build());

            orderRepository.save(opposingOrder);
        }

        orderRepository.save(newOrder);
    }

    private List<Order> getOpposingOrders(Order newOrder) {
        if (newOrder.getSide() == OrderSide.BUY) {
            return orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceAscTimestampAsc(
                    newOrder.getInstrument(),
                    OrderSide.SELL,
                    ACTIVE_STATUSES);
        }

        return orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceDescTimestampAsc(
                newOrder.getInstrument(),
                OrderSide.BUY,
                ACTIVE_STATUSES);
    }

    private boolean isMatch(Order newOrder, Order opposingOrder) {
        if (newOrder.getOrderType() == OrderType.MARKET) {
            return true;
        }

        if (newOrder.getSide() == OrderSide.BUY) {
            return newOrder.getPrice().compareTo(opposingOrder.getPrice()) >= 0;
        }

        return newOrder.getPrice().compareTo(opposingOrder.getPrice()) <= 0;
    }

    private OrderStatus determineStatus(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0
                ? OrderStatus.PARTIALLY_FILLED
                : OrderStatus.FILLED;
    }

    private java.util.UUID resolveBuyOrderId(Order newOrder, Order opposingOrder) {
        return newOrder.getSide() == OrderSide.BUY ? newOrder.getOrderId() : opposingOrder.getOrderId();
    }

    private java.util.UUID resolveSellOrderId(Order newOrder, Order opposingOrder) {
        return newOrder.getSide() == OrderSide.SELL ? newOrder.getOrderId() : opposingOrder.getOrderId();
    }
}