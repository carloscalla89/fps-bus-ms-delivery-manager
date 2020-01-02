package com.inretailpharma.digital.ordermanager.canonical.dispatcher;

public class TrackerResponseDto {

    private Long orderExternalId;
    private String detail;

    public Long getOrderExternalId() {
        return orderExternalId;
    }

    public void setOrderExternalId(Long orderExternalId) {
        this.orderExternalId = orderExternalId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "TrackerResponseDto{" +
                "orderExternalId=" + orderExternalId +
                ", detail='" + detail + '\'' +
                '}';
    }
}
