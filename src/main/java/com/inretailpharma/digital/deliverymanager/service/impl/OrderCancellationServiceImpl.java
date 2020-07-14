package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.OrderCancelled;
import com.inretailpharma.digital.deliverymanager.repository.CancellationCodeReasonRepository;
import com.inretailpharma.digital.deliverymanager.repository.OrderCancelledRepository;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderCancellationServiceImpl implements OrderCancellationService {

    private CancellationCodeReasonRepository cancellationCodeReasonRepository;
    private OrderCancelledRepository orderCancelledRepository;

    public OrderCancellationServiceImpl(CancellationCodeReasonRepository cancellationCodeReasonRepository,
                                        OrderCancelledRepository orderCancelledRepository) {
        this.cancellationCodeReasonRepository = cancellationCodeReasonRepository;
        this.orderCancelledRepository = orderCancelledRepository;
    }

    @Override
    public List<CancellationCodeReason> getListCodeCancellationByCode(String appType) {
        return cancellationCodeReasonRepository.findAllByAppType(appType);
    }

    @Override
    public void insertCancelledOrder(OrderCancelled orderCancelled) {
        orderCancelledRepository.save(orderCancelled);
    }

    @Override
    public CancellationCodeReason geByCode(String code) {
        return cancellationCodeReasonRepository.getOne(code);
    }
}
