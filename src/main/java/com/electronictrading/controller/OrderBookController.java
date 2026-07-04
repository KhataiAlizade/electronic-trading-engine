package com.electronictrading.controller;

import com.electronictrading.dto.OrderBookEntryDTO;
import com.electronictrading.service.OrderBookService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orderbook")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping("/{instrument}")
    public Map<String, List<OrderBookEntryDTO>> getOrderBook(@PathVariable String instrument) {
        return orderBookService.getOrderBook(instrument);
    }
}