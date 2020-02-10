package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.OrderCancelled;

import java.util.List;

public interface OrderCancellationService {

    List<CancellationCodeReason> getListCodeCancellationByCode();
    void insertCancelledOrder(OrderCancelled orderCancelled);
    CancellationCodeReason geByCode(String code);
}
