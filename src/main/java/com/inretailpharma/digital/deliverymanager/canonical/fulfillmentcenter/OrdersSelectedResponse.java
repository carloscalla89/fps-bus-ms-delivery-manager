package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderHeaderDetail;
import com.inretailpharma.digital.deliverymanager.dto.OrderInfoConsolidated;
import lombok.Data;

import java.util.List;

@Data
public class OrdersSelectedResponse {
    private List<OrderHeaderDetail> orders;

}
