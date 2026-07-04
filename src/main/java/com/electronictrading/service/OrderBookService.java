package com.electronictrading.service;

import com.electronictrading.dto.OrderBookEntryDTO;
import com.electronictrading.model.Order;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import com.electronictrading.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OrderBookService {

    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.NEW,
            OrderStatus.PARTIALLY_FILLED);

    private final OrderRepository orderRepository;

    public OrderBookService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Map<String, List<OrderBookEntryDTO>> getOrderBook(String instrument) {
        List<Order> bids = orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceDescTimestampAsc(
                instrument,
                OrderSide.BUY,
                ACTIVE_STATUSES);
        List<Order> asks = orderRepository.findByInstrumentAndSideAndStatusInOrderByPriceAscTimestampAsc(
                instrument,
                OrderSide.SELL,
                ACTIVE_STATUSES);

        Map<String, List<OrderBookEntryDTO>> orderBook = new LinkedHashMap<>();
        orderBook.put("bids", aggregateByPrice(bids, Comparator.reverseOrder()));
        orderBook.put("asks", aggregateByPrice(asks, Comparator.naturalOrder()));
        return orderBook;
    }

    private List<OrderBookEntryDTO> aggregateByPrice(List<Order> orders, Comparator<BigDecimal> priceOrder) {
        Map<BigDecimal, BigDecimal> aggregated = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getPrice,
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Order::getQuantity, BigDecimal::add)));

        List<BigDecimal> prices = new ArrayList<>(aggregated.keySet());
        prices.sort(priceOrder);

        return prices.stream()
                .map(price -> new OrderBookEntryDTO(price, aggregated.get(price)))
                .toList();
    }
}