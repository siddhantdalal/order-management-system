package com.orderflow.orderservice.service;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEvent(OrderEvent event) {
        log.info("Publishing order event: type={}, orderId={}", event.getEventType(), event.getOrder().getId());
        kafkaTemplate.send(KafkaConstants.ORDER_EVENTS_TOPIC, String.valueOf(event.getOrder().getId()), event);
    }
}
