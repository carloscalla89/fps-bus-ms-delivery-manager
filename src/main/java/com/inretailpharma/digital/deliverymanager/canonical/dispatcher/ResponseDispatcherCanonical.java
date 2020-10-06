package com.inretailpharma.digital.deliverymanager.canonical.dispatcher;

import lombok.Data;

@Data
public class ResponseDispatcherCanonical{

    private InsinkResponseCanonical body;
    private StatusDispatcher status;

}
