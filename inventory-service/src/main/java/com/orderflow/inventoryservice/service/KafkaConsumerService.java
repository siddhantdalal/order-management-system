package com.orderflow.inventoryservice.service;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.OrderEvent;
import com.orderflow.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final InventoryService inventoryService;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = KafkaConstants.PAYMENT_EVENTS_TOPIC, groupId = KafkaConstants.INVENTORY_GROUP)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        if (event.getEventType() == PaymentEvent.Type.PAYMENT_COMPLETED) {
            // Need to get order items from the payment event context
            // For now, publish stock reserved/unavailable based on reservation result
            // The order items will come through the event chain
            log.info("Payment completed for order: {}, stock reservation handled via order context", event.getOrderId());
        }
    }

    @KafkaListener(topics = KafkaConstants.ORDER_EVENTS_TOPIC, groupId = KafkaConstants.INVENTORY_GROUP)
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrder().getId());

        if (event.getEventType() == OrderEvent.Type.ORDER_PLACED) {
            boolean reserved = inventoryService.reserveStock(
                    event.getOrder().getId(),
                    event.getOrder().getItems());

            InventoryEvent inventoryEvent = InventoryEvent.builder()
                    .eventType(reserved ? InventoryEvent.Type.STOCK_RESERVED : InventoryEvent.Type.STOCK_UNAVAILABLE)
                    .orderId(event.getOrder().getId())
                    .items(event.getOrder().getItems())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.sendInventoryEvent(inventoryEvent);
        } else if (event.getEventType() == OrderEvent.Type.ORDER_CANCELLED) {
            inventoryService.releaseStock(event.getOrder().getId());

            kafkaProducerService.sendInventoryEvent(InventoryEvent.builder()
                    .eventType(InventoryEvent.Type.STOCK_RELEASED)
                    .orderId(event.getOrder().getId())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}
