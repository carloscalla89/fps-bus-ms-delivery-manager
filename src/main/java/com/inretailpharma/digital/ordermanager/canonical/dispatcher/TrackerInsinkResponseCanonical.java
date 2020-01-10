package com.inretailpharma.digital.ordermanager.canonical.dispatcher;

import java.io.Serializable;

/**
 * @author Henry Gonzales Segovia
 * @version 9/01/2018
 */
public class TrackerInsinkResponseCanonical implements Serializable {

    private Boolean insinkProcess;
    private Boolean trackerProcess;
    private InsinkResponseCanonical insinkResponseCanonical;
    private TrackerResponseDto trackerResponseDto;
    private boolean released;

    public Boolean getInsinkProcess() {
        return insinkProcess;
    }

    public void setInsinkProcess(Boolean insinkProcess) {
        this.insinkProcess = insinkProcess;
    }

    public Boolean getTrackerProcess() {
        return trackerProcess;
    }

    public void setTrackerProcess(Boolean trackerProcess) {
        this.trackerProcess = trackerProcess;
    }

    public InsinkResponseCanonical getInsinkResponseCanonical() {
        return insinkResponseCanonical;
    }

    public void setInsinkResponseCanonical(InsinkResponseCanonical insinkResponseCanonical) {
        this.insinkResponseCanonical = insinkResponseCanonical;
    }

    public TrackerResponseDto getTrackerResponseDto() {
        return trackerResponseDto;
    }

    public void setTrackerResponseDto(TrackerResponseDto trackerResponseDto) {
        this.trackerResponseDto = trackerResponseDto;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    @Override
    public String toString() {
        return "TrackerInsinkResponseCanonical{" +
                "insinkProcess=" + insinkProcess +
                ", trackerProcess=" + trackerProcess +
                ", insinkResponseCanonical=" + insinkResponseCanonical +
                ", trackerResponseDto=" + trackerResponseDto +
                ", released=" + released +
                '}';
    }
}
