package com.orderflow.orderservice.service;

import com.orderflow.common.dto.OrderDto;
import com.orderflow.orderservice.dto.CreateOrderRequest;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(Long userId, CreateOrderRequest request);
    OrderDto getById(Long id);
    List<OrderDto> getByUserId(Long userId);
    List<OrderDto> getAllOrders();
    OrderDto updateStatus(Long id, String status);
    void cancelOrder(Long id);
}
