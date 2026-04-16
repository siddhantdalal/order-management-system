package com.orderflow.orderservice.service;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.PaymentEvent;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderStatus;
import com.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = KafkaConstants.PAYMENT_EVENTS_TOPIC, groupId = KafkaConstants.ORDER_GROUP)
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        if (event.getEventType() == PaymentEvent.Type.PAYMENT_FAILED) {
            updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
        }
    }

    @KafkaListener(topics = KafkaConstants.INVENTORY_EVENTS_TOPIC, groupId = KafkaConstants.ORDER_GROUP)
    @Transactional
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        if (event.getEventType() == InventoryEvent.Type.STOCK_RESERVED) {
            updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
        } else if (event.getEventType() == InventoryEvent.Type.STOCK_UNAVAILABLE) {
            updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
        }
    }

    private void updateOrderStatus(Long orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            log.info("Order {} status updated to {}", orderId, status);
        });
    }
}
