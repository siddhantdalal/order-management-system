package com.orderflow.common.event;

import com.orderflow.common.dto.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;
}
