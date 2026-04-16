package com.orderflow.paymentservice.service;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaConstants.ORDER_EVENTS_TOPIC, groupId = KafkaConstants.PAYMENT_GROUP)
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrder().getId());

        if (event.getEventType() == OrderEvent.Type.ORDER_PLACED) {
            paymentService.processPayment(
                    event.getOrder().getId(),
                    event.getOrder().getUserId(),
                    event.getOrder().getTotalAmount(),
                    "CREDIT_CARD",
                    event.getOrder().getItems()
            );
        }
    }

    @KafkaListener(topics = KafkaConstants.INVENTORY_EVENTS_TOPIC, groupId = KafkaConstants.PAYMENT_GROUP)
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        if (event.getEventType() == InventoryEvent.Type.STOCK_UNAVAILABLE) {
            paymentService.refundPayment(event.getOrderId());
        }
    }
}
