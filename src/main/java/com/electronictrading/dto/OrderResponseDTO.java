package com.electronictrading.dto;

import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponseDTO(
        UUID orderId,
        String instrument,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        Instant timestamp) {
}