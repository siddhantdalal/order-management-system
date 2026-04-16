package com.orderflow.common.constants;

public final class KafkaConstants {

    private KafkaConstants() {}

    // Topics
    public static final String ORDER_EVENTS_TOPIC = "order-events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    // Consumer Groups
    public static final String PAYMENT_GROUP = "payment-group";
    public static final String INVENTORY_GROUP = "inventory-group";
    public static final String ORDER_GROUP = "order-group";
    public static final String NOTIFICATION_GROUP = "notification-group";
}
