package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import java.util.List;

public interface OrderCancellationService {

    List<CancellationCodeReason> getListCodeCancellationByAppTypeList(List<String> appType);
    CancellationCodeReason geByCode(String code);
    CancellationCodeReason geByCodeAndAppType(String code, String appType);
    List<CancellationCodeReason> getListCodeCancellationByAppTypeListAndType(List<String> appType, String type);

    default CancellationCodeReason evaluateGetCancel(ActionDto actionDto) {

        CancellationCodeReason cancellationCodeReason;

        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())
                || Constant.ActionOrder.REJECT_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
            if (actionDto.getOrderCancelCode() != null && actionDto.getOrigin() != null) {
                cancellationCodeReason = geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrigin());
            } else {
                cancellationCodeReason = geByCode(actionDto.getOrderCancelCode());
            }
        } else {
            cancellationCodeReason = null;
        }

        return cancellationCodeReason;

    }



}
