package com.orderflow.notificationservice.service;

import com.orderflow.common.event.InventoryEvent;
import com.orderflow.common.event.OrderEvent;
import com.orderflow.common.event.PaymentEvent;
import com.orderflow.notificationservice.entity.Notification;
import com.orderflow.notificationservice.entity.NotificationStatus;
import com.orderflow.notificationservice.entity.NotificationType;
import com.orderflow.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public void processOrderEvent(OrderEvent event) {
        NotificationType type = mapOrderEventType(event.getEventType());
        if (type == null) return;

        String subject = generateOrderSubject(event);
        String content = generateOrderContent(event);

        Notification notification = createAndSaveNotification(
                event.getOrder().getUserId(),
                event.getOrder().getId(),
                type, subject, content);

        emailService.sendEmail(notification.getRecipientEmail(), subject, content);
        log.info("Order notification sent: type={}, orderId={}", type, event.getOrder().getId());
    }

    @Override
    public void processPaymentEvent(PaymentEvent event) {
        NotificationType type = mapPaymentEventType(event.getEventType());
        if (type == null) return;

        String subject = "Payment " + event.getEventType().name().replace("PAYMENT_", "").toLowerCase()
                + " for Order #" + event.getOrderId();
        String content = String.format("Your payment of $%s for Order #%d has been %s. Transaction ID: %s",
                event.getPayment().getAmount(),
                event.getOrderId(),
                event.getEventType().name().replace("PAYMENT_", "").toLowerCase(),
                event.getPayment().getTransactionId());

        Notification notification = createAndSaveNotification(
                null, event.getOrderId(), type, subject, content);

        emailService.sendEmail(notification.getRecipientEmail(), subject, content);
        log.info("Payment notification sent: type={}, orderId={}", type, event.getOrderId());
    }

    @Override
    public void processInventoryEvent(InventoryEvent event) {
        if (event.getEventType() == InventoryEvent.Type.STOCK_UNAVAILABLE) {
            String subject = "Stock unavailable for Order #" + event.getOrderId();
            String content = String.format("We're sorry, one or more items in Order #%d are currently out of stock. Your order has been cancelled and a refund will be processed.",
                    event.getOrderId());

            createAndSaveNotification(null, event.getOrderId(),
                    NotificationType.ORDER_CANCELLED, subject, content);

            log.info("Inventory notification sent: orderId={}", event.getOrderId());
        }
    }

    @Override
    public List<Notification> getByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getByOrderId(Long orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    private Notification createAndSaveNotification(Long userId, Long orderId,
                                                    NotificationType type, String subject, String content) {
        Notification notification = Notification.builder()
                .userId(userId != null ? userId : 0L)
                .orderId(orderId)
                .type(type)
                .channel("EMAIL")
                .subject(subject)
                .content(content)
                .status(NotificationStatus.SENT)
                .recipientEmail("customer@orderflow.com")
                .build();

        return notificationRepository.save(notification);
    }

    private NotificationType mapOrderEventType(OrderEvent.Type eventType) {
        return switch (eventType) {
            case ORDER_PLACED -> NotificationType.ORDER_PLACED;
            case ORDER_CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case ORDER_SHIPPED -> NotificationType.ORDER_SHIPPED;
            case ORDER_DELIVERED -> NotificationType.ORDER_DELIVERED;
            case ORDER_CANCELLED -> NotificationType.ORDER_CANCELLED;
        };
    }

    private NotificationType mapPaymentEventType(PaymentEvent.Type eventType) {
        return switch (eventType) {
            case PAYMENT_COMPLETED -> NotificationType.PAYMENT_COMPLETED;
            case PAYMENT_FAILED -> NotificationType.PAYMENT_FAILED;
            case PAYMENT_REFUNDED -> null;
        };
    }

    private String generateOrderSubject(OrderEvent event) {
        return switch (event.getEventType()) {
            case ORDER_PLACED -> "Order #" + event.getOrder().getId() + " placed successfully";
            case ORDER_CONFIRMED -> "Order #" + event.getOrder().getId() + " has been confirmed";
            case ORDER_SHIPPED -> "Order #" + event.getOrder().getId() + " has been shipped";
            case ORDER_DELIVERED -> "Order #" + event.getOrder().getId() + " has been delivered";
            case ORDER_CANCELLED -> "Order #" + event.getOrder().getId() + " has been cancelled";
        };
    }

    private String generateOrderContent(OrderEvent event) {
        return switch (event.getEventType()) {
            case ORDER_PLACED -> String.format("Your order #%d for $%s has been placed and is being processed.",
                    event.getOrder().getId(), event.getOrder().getTotalAmount());
            case ORDER_CONFIRMED -> String.format("Great news! Your order #%d has been confirmed and is being prepared for shipment.",
                    event.getOrder().getId());
            case ORDER_SHIPPED -> String.format("Your order #%d has been shipped! You can track your delivery status.",
                    event.getOrder().getId());
            case ORDER_DELIVERED -> String.format("Your order #%d has been delivered. Thank you for shopping with us!",
                    event.getOrder().getId());
            case ORDER_CANCELLED -> String.format("Your order #%d has been cancelled. If a payment was made, a refund will be processed.",
                    event.getOrder().getId());
        };
    }
}
