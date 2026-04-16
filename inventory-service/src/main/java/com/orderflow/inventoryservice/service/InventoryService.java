package com.orderflow.inventoryservice.service;

import com.orderflow.common.dto.OrderItemDto;

import java.util.List;

public interface InventoryService {
    boolean reserveStock(Long orderId, List<OrderItemDto> items);
    void releaseStock(Long orderId);
}
