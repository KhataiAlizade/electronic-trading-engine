package com.electronictrading.repository;

import com.electronictrading.model.Order;
import com.electronictrading.model.OrderSide;
import com.electronictrading.model.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserId(String userId);

    List<Order> findByInstrumentAndSideAndStatusInOrderByPriceDescTimestampAsc(
            String instrument,
            OrderSide side,
            List<OrderStatus> statuses);

    List<Order> findByInstrumentAndSideAndStatusInOrderByPriceAscTimestampAsc(
            String instrument,
            OrderSide side,
            List<OrderStatus> statuses);
}