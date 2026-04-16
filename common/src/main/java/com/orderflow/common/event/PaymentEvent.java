package com.orderflow.common.event;

import com.orderflow.common.dto.OrderItemDto;
import com.orderflow.common.dto.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    public enum Type {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED
    }

    private Type eventType;
    private PaymentDto payment;
    private Long orderId;
    private List<OrderItemDto> orderItems;
    private LocalDateTime timestamp;
}
