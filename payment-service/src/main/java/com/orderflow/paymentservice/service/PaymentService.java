package com.orderflow.paymentservice.service;

import com.orderflow.common.dto.OrderItemDto;
import com.orderflow.common.dto.PaymentDto;

import java.util.List;

public interface PaymentService {
    PaymentDto processPayment(Long orderId, Long userId, java.math.BigDecimal amount, String paymentMethod, List<OrderItemDto> orderItems);
    PaymentDto refundPayment(Long orderId);
    PaymentDto getByOrderId(Long orderId);
    List<PaymentDto> getByUserId(Long userId);
}
