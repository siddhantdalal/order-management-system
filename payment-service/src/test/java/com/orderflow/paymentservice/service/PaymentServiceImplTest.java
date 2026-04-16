package com.orderflow.paymentservice.service;

import com.orderflow.common.dto.PaymentDto;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.paymentservice.entity.Payment;
import com.orderflow.paymentservice.entity.PaymentMethod;
import com.orderflow.paymentservice.entity.PaymentStatus;
import com.orderflow.paymentservice.mapper.PaymentMapper;
import com.orderflow.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_SmallAmount_AlwaysSucceeds() {
        Payment savedPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(new BigDecimal("50.00"))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionId("txn-123")
                .build();

        PaymentDto paymentDto = PaymentDto.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .status("COMPLETED")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentDto);

        PaymentDto result = paymentService.processPayment(1L, 1L, new BigDecimal("50.00"), "CREDIT_CARD");

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(kafkaProducerService).sendPaymentEvent(any());
    }

    @Test
    void refundPayment_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionId("txn-123")
                .build();

        PaymentDto paymentDto = PaymentDto.builder()
                .id(1L)
                .orderId(1L)
                .status("REFUNDED")
                .build();

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(paymentDto);

        PaymentDto result = paymentService.refundPayment(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        verify(kafkaProducerService).sendPaymentEvent(any());
    }

    @Test
    void refundPayment_NotFound_ThrowsException() {
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.refundPayment(999L));
    }
}
