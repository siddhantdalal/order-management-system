package com.orderflow.paymentservice.service;

import com.orderflow.common.dto.OrderItemDto;
import com.orderflow.common.dto.PaymentDto;
import com.orderflow.common.event.PaymentEvent;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.paymentservice.entity.Payment;
import com.orderflow.paymentservice.entity.PaymentMethod;
import com.orderflow.paymentservice.entity.PaymentStatus;
import com.orderflow.paymentservice.mapper.PaymentMapper;
import com.orderflow.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaProducerService kafkaProducerService;

    private static final BigDecimal MAX_AUTO_APPROVE_AMOUNT = new BigDecimal("10000");

    @Override
    @Transactional
    public PaymentDto processPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod, List<OrderItemDto> orderItems) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .paymentMethod(PaymentMethod.valueOf(paymentMethod))
                .status(PaymentStatus.PENDING)
                .transactionId(UUID.randomUUID().toString())
                .build();

        boolean success = simulatePayment(amount);
        payment.setStatus(success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        Payment savedPayment = paymentRepository.save(payment);

        PaymentDto paymentDto = paymentMapper.toDto(savedPayment);
        PaymentEvent.Type eventType = success
                ? PaymentEvent.Type.PAYMENT_COMPLETED
                : PaymentEvent.Type.PAYMENT_FAILED;

        kafkaProducerService.sendPaymentEvent(PaymentEvent.builder()
                .eventType(eventType)
                .payment(paymentDto)
                .orderId(orderId)
                .orderItems(orderItems)
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Payment processed: orderId={}, status={}, transactionId={}",
                orderId, savedPayment.getStatus(), savedPayment.getTransactionId());
        return paymentDto;
    }

    @Override
    @Transactional
    public PaymentDto refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);

        PaymentDto paymentDto = paymentMapper.toDto(savedPayment);
        kafkaProducerService.sendPaymentEvent(PaymentEvent.builder()
                .eventType(PaymentEvent.Type.PAYMENT_REFUNDED)
                .payment(paymentDto)
                .orderId(orderId)
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Payment refunded: orderId={}, transactionId={}", orderId, payment.getTransactionId());
        return paymentDto;
    }

    @Override
    public PaymentDto getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDto> getByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    private boolean simulatePayment(BigDecimal amount) {
        if (amount.compareTo(MAX_AUTO_APPROVE_AMOUNT) < 0) {
            return true;
        }
        return Math.random() > 0.3;
    }
}
