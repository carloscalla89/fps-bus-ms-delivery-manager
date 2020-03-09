package com.inretailpharma.digital.deliverymanager.canonical.dispatcher;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrackerInsinkResponseCanonical implements Serializable {

    private Boolean insinkProcess;
    private Boolean trackerProcess;
    private InsinkResponseCanonical insinkResponseCanonical;
    private TrackerResponseDto trackerResponseDto;
    private boolean released;

}
