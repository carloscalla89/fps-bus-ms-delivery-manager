package com.inretailpharma.digital.deliverymanager.canonical.dispatcher;

import lombok.Data;

@Data
public class ResponseDispatcherCanonical<R, S> {

    private R body;
    private S status;

}
