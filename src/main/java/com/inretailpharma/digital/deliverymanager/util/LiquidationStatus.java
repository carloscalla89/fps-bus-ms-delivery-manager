package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;

@FunctionalInterface
public interface LiquidationStatus {

    StatusDto process(String liquidationStatus, String firstDigitalStatus, String action, String orderCancelCode,
                      String serviceType);

}
