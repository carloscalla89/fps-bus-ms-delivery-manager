package com.inretailpharma.digital.deliverymanager.pubsub;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface OrderStatusService {

    String CHANNEL = "channel";

    @Input(CHANNEL)
    SubscribableChannel inputSubscribable();
}
