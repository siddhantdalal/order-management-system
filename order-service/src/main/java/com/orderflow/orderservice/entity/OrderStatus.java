package com.orderflow.orderservice.entity;

public enum OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
