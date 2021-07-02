package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.LiquidationAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component("retryLiquidation")
public class LiquidationOrder extends FacadeAbstractUtil implements IActionStrategy {

    private LiquidationAdapter iLiquidationAdapter;
    private OrderTransaction orderTransaction;

    @Autowired
    public LiquidationOrder(LiquidationAdapter iLiquidationAdapter, OrderTransaction orderTransaction) {
        this.iLiquidationAdapter = iLiquidationAdapter;
        this.orderTransaction = orderTransaction;
    }

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {
        return Optional
                .ofNullable(getOnlyOrderByecommerceId(ecommerceId))
                .filter(val -> val.getLiquidationStatusDetail() != null
                        && !Constant.LiquidationStatus.getStatusSuccessByErrorStatus(val.getLiquidationStatus()).equals(Constant.LiquidationStatus.NOT_FOUND_CODE))
                .isPresent();
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        log.info("[START] init process retry liquidations");
        IOrderFulfillment iOrderFulfillment = getOrderLightByecommerceId(ecommerceId);

        OrderCanonical orderCanonical = new OrderCanonical();
        orderCanonical.setId(iOrderFulfillment.getOrderId());
        orderCanonical.setEcommerceId(ecommerceId);

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCancellationCode(iOrderFulfillment.getCancellationCode());
        orderStatus.setCancellationDescription(iOrderFulfillment.getCancellationDescription());

        orderCanonical.setOrderStatus(orderStatus);

        Constant.LiquidationStatus liquidationStatus =
                Constant.LiquidationStatus.getByStatusByName(iOrderFulfillment.getLiquidationStatus());

        if (liquidationStatus.getMethod().equalsIgnoreCase(Constant.METHOD_CREATE)) {


            orderCanonical.setCompanyCode(iOrderFulfillment.getCompanyCode());
            orderCanonical.setLocalCode(iOrderFulfillment.getCenterCode());
            orderCanonical.setSource(iOrderFulfillment.getSource());
            orderCanonical.setTotalAmount(iOrderFulfillment.getTotalCost());

            PaymentMethodCanonical paymentMethod = new PaymentMethodCanonical();
            paymentMethod.setPurchaseNumber(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).map(Object::toString).orElse(null));
            paymentMethod.setPaymentTransactionId(iOrderFulfillment.getPaymentTransactionId());

            paymentMethod.setTransactionDateVisanet(
                    Optional.ofNullable(iOrderFulfillment.getTransactionDateVisanet()).map(DateUtils::getLocalDateTimeWithFormat).orElse(null)
            );

            paymentMethod.setChangeAmount(iOrderFulfillment.getChangeAmount());
            paymentMethod.setType(iOrderFulfillment.getPaymentType());
            paymentMethod.setCardProvider(iOrderFulfillment.getCardProvider());
            paymentMethod.setCardProviderCode(iOrderFulfillment.getCardProviderCode());
            paymentMethod.setPaidAmount(iOrderFulfillment.getPaidAmount());

            orderCanonical.setPaymentMethod(paymentMethod);

            OrderDetailCanonical orderDetailCanonical = new OrderDetailCanonical();
            orderDetailCanonical.setServiceSourceChannel(iOrderFulfillment.getServiceChannel());
            orderDetailCanonical.setServiceType(iOrderFulfillment.getServiceType());
            orderDetailCanonical.setServiceShortCode(iOrderFulfillment.getServiceTypeShortCode());
            orderCanonical.setOrderDetail(orderDetailCanonical);

            StoreCenterCanonical storeCenter = new StoreCenterCanonical();
            storeCenter.setLocalType(
                    Constant.TrackerImplementation.getClassImplement(iOrderFulfillment.getClassImplement()).getLocalType());

            orderCanonical.setStoreCenter(storeCenter);

            ClientCanonical client = new ClientCanonical();
            client.setFullName(Optional.ofNullable(iOrderFulfillment.getLastName()).orElse(StringUtils.EMPTY)
                    + StringUtils.SPACE
                    + Optional.ofNullable(iOrderFulfillment.getFirstName()).orElse(StringUtils.EMPTY));
            client.setDocumentNumber(iOrderFulfillment.getDocumentNumber());
            client.setPhone(iOrderFulfillment.getPhone());

            orderCanonical.setClient(client);



            return iLiquidationAdapter
                    .createOrder(
                            orderCanonical, LiquidationCanonical
                                    .builder()
                                    .enabled(true)
                                    .code(liquidationStatus.getCode())
                                    .status(liquidationStatus.name())
                                    .build()
                    )
                    .flatMap(resultOrder -> {

                        orderTransaction.updateLiquidationStatusOrder(
                                resultOrder.getLiquidation().getStatus(), resultOrder.getLiquidation().getDetail(), orderCanonical.getId()
                        );

                        return Mono.just(resultOrder);
                    });

        } else {

            return iLiquidationAdapter
                    .updateOrder(orderCanonical, LiquidationCanonical
                            .builder()
                            .enabled(true)
                            .code(liquidationStatus.getCode())
                            .status(liquidationStatus.name())
                            .build(), actionDto.getOrigin())
                    .flatMap(resultOrder -> {

                        orderTransaction.updateLiquidationStatusOrder(
                                resultOrder.getLiquidation().getStatus(), resultOrder.getLiquidation().getDetail(), orderCanonical.getId()
                        );

                        return Mono.just(resultOrder);
                    });

        }



    }
}
