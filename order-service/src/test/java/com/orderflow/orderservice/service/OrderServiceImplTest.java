package com.orderflow.orderservice.service;

import com.orderflow.common.dto.OrderDto;
import com.orderflow.common.exception.BadRequestException;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.orderservice.dto.CreateOrderRequest;
import com.orderflow.orderservice.dto.OrderItemRequest;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderItem;
import com.orderflow.orderservice.entity.OrderStatus;
import com.orderflow.orderservice.mapper.OrderMapper;
import com.orderflow.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("99.99"))
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Test St")
                .items(List.of(OrderItem.builder()
                        .productId(1L)
                        .productName("Test Product")
                        .quantity(1)
                        .unitPrice(new BigDecimal("99.99"))
                        .build()))
                .createdAt(LocalDateTime.now())
                .build();

        testOrderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(new BigDecimal("99.99"))
                .status("PENDING")
                .shippingAddress("123 Test St")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(1L, "Test Product", 1, new BigDecimal("99.99"))),
                "123 Test St"
        );

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderDto);

        OrderDto result = orderService.createOrder(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(kafkaProducerService).sendOrderEvent(any());
    }

    @Test
    void getById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDto);

        OrderDto result = orderService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getById(999L));
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(any(Order.class))).thenReturn(testOrderDto);

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(kafkaProducerService).sendOrderEvent(any());
    }

    @Test
    void cancelOrder_DeliveredOrder_ThrowsException() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(1L));
    }
}
