package com.electronictrading.controller;

import com.electronictrading.dto.OrderRequestDTO;
import com.electronictrading.dto.OrderResponseDTO;
import com.electronictrading.service.OrderManagementService;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderManagementService orderManagementService;

    public OrderController(OrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @PostMapping
    public OrderResponseDTO placeOrder(@Valid @RequestBody OrderRequestDTO orderRequest) {
        return orderManagementService.placeOrder(orderRequest);
    }

    @DeleteMapping("/{id}")
    public OrderResponseDTO cancelOrder(@PathVariable UUID id) {
        return orderManagementService.cancelOrder(id);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrder(@PathVariable UUID id) {
        return orderManagementService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponseDTO> getOrdersByUser(@RequestParam("userId") String userId) {
        return orderManagementService.getOrdersByUser(userId);
    }
}