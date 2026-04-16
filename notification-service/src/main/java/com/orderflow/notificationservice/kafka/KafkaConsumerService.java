package com.orderflow.notificationservice.kafka;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.OrderEvent;
import com.orderflow.common.event.PaymentEvent;
import com.orderflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaConstants.ORDER_EVENTS_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event for notification: type={}, orderId={}",
                event.getEventType(), event.getOrder().getId());
        notificationService.processOrderEvent(event);
    }

    @KafkaListener(topics = KafkaConstants.PAYMENT_EVENTS_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event for notification: type={}, orderId={}",
                event.getEventType(), event.getOrderId());
        notificationService.processPaymentEvent(event);
    }

    @KafkaListener(topics = KafkaConstants.INVENTORY_EVENTS_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event for notification: type={}, orderId={}",
                event.getEventType(), event.getOrderId());
        notificationService.processInventoryEvent(event);
    }
}
