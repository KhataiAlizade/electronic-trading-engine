package com.electronictrading.dto;

import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderType;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequestDTO(
        @NotNull String instrument,
        @NotNull OrderSide side,
        @NotNull @Positive BigDecimal quantity,
        @Positive BigDecimal price,
        @NotNull OrderType type) {
}