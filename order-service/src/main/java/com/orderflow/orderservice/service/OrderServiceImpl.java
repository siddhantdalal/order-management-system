package com.orderflow.orderservice.service;

import com.orderflow.common.dto.OrderDto;
import com.orderflow.common.event.OrderEvent;
import com.orderflow.common.exception.BadRequestException;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.orderservice.dto.CreateOrderRequest;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderItem;
import com.orderflow.orderservice.entity.OrderStatus;
import com.orderflow.orderservice.mapper.OrderMapper;
import com.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public OrderDto createOrder(Long userId, CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(order)
                        .productId(itemReq.getProductId())
                        .productName(itemReq.getProductName())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .build())
                .toList();

        order.setItems(items);

        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        OrderDto orderDto = orderMapper.toDto(savedOrder);

        kafkaProducerService.sendOrderEvent(OrderEvent.builder()
                .eventType(OrderEvent.Type.ORDER_PLACED)
                .order(orderDto)
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Order created: id={}, userId={}, total={}", savedOrder.getId(), userId, total);
        return orderDto;
    }

    @Override
    @Cacheable(value = "orders", key = "#id", unless = "#result == null")
    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderDto updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        OrderStatus newStatus = OrderStatus.valueOf(status);
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        OrderDto orderDto = orderMapper.toDto(updatedOrder);
        OrderEvent.Type eventType = mapStatusToEventType(newStatus);
        if (eventType != null) {
            kafkaProducerService.sendOrderEvent(OrderEvent.builder()
                    .eventType(eventType)
                    .order(orderDto)
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        log.info("Order status updated: id={}, status={}", id, status);
        return orderDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        OrderDto orderDto = orderMapper.toDto(order);
        kafkaProducerService.sendOrderEvent(OrderEvent.builder()
                .eventType(OrderEvent.Type.ORDER_CANCELLED)
                .order(orderDto)
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Order cancelled: id={}", id);
    }

    private OrderEvent.Type mapStatusToEventType(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> OrderEvent.Type.ORDER_CONFIRMED;
            case SHIPPED -> OrderEvent.Type.ORDER_SHIPPED;
            case DELIVERED -> OrderEvent.Type.ORDER_DELIVERED;
            case CANCELLED -> OrderEvent.Type.ORDER_CANCELLED;
            default -> null;
        };
    }
}
