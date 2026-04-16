package com.orderflow.common.event;

import com.orderflow.common.dto.OrderItemDto;
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
public class InventoryEvent {

    public enum Type {
        STOCK_RESERVED,
        STOCK_UNAVAILABLE,
        STOCK_RELEASED
    }

    private Type eventType;
    private Long orderId;
    private List<OrderItemDto> items;
    private LocalDateTime timestamp;
}
