package com.orderflow.paymentservice.service;

import com.orderflow.common.constants.KafkaConstants;
import com.orderflow.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Publishing payment event: type={}, orderId={}", event.getEventType(), event.getOrderId());
        kafkaTemplate.send(KafkaConstants.PAYMENT_EVENTS_TOPIC, String.valueOf(event.getOrderId()), event);
    }
}
