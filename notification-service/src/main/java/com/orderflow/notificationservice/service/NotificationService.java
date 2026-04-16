package com.orderflow.notificationservice.service;

import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.OrderEvent;
import com.orderflow.common.event.PaymentEvent;
import com.orderflow.notificationservice.entity.Notification;

import java.util.List;

public interface NotificationService {
    void processOrderEvent(OrderEvent event);
    void processPaymentEvent(PaymentEvent event);
    void processInventoryEvent(InventoryEvent event);
    List<Notification> getByUserId(Long userId);
    List<Notification> getByOrderId(Long orderId);
}
