package com.inretailpharma.digital.deliverymanager.dto;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfo;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoAdditional;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoClient;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoPaymentMethodDto;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoProduct;
import lombok.Data;

@Data
public class OrderInfoConsolidated {

  private OrderInfo orderInfo;
  private OrderInfoClient orderInfoClient;
  private OrderInfoAdditional orderInfoAdditional;
  private OrderInfoPaymentMethodDto paymentMethodDto;
  private OrderInfoProduct productDetail;
}
