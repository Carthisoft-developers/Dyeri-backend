// com/dyeri/events/KafkaTopics.java
package com.dyeri.events;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String ORDERS_PLACED          = "dyeri.orders.placed";
    public static final String ORDERS_STATUS_CHANGED  = "dyeri.orders.status-changed";
    public static final String PAYMENTS_CONFIRMED     = "dyeri.payments.confirmed";
    public static final String PAYMENTS_FAILED        = "dyeri.payments.failed";
    public static final String NOTIFICATIONS_SEND     = "dyeri.notifications.send";
}
