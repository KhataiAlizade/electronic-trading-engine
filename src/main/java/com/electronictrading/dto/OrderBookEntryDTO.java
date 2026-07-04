package com.electronictrading.dto;

import java.math.BigDecimal;

public record OrderBookEntryDTO(BigDecimal price, BigDecimal quantity) {
}