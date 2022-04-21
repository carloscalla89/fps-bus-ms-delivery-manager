package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderHeaderDetail;
import lombok.Data;

import java.util.List;

@Data
public class OrdersSelectedResponse {
    private List<OrderCanonicalFulfitment> orders;
    private List<OrderHeaderDetail> listOrder;
}
