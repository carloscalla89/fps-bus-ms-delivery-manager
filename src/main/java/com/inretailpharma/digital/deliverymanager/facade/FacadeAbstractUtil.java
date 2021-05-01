package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.INotificationAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class FacadeAbstractUtil {

    @Autowired
    private OrderTransaction orderTransaction;

    @Autowired
    private ObjectToMapper objectToMapper;

    @Autowired
    private ApplicationParameterService applicationParameterService;

    protected List<IOrderFulfillment> getListOrdersToCancel(String serviceType, String companyCode, Integer maxDayPickup,
                                                            String statustype) {
        return orderTransaction.getListOrdersToCancel(serviceType, companyCode, maxDayPickup, statustype);
    }

    protected String getValueOfParameter(String parameter) {
        return applicationParameterService.getApplicationParameterByCodeIs(parameter).getValue();
    }

    protected List<CancellationCanonical> getCancellationsCodeByAppType(String appType) {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason(appType));
    }

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical, Long id, Long ecommerceId,
                                                            Long externalId, String orderCancelCode,
                                                            String orderCancelObservation, String source, String target,
                                                            String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation, orderCancelCode,
                orderCanonical.getOrderStatus().getCode(), id, localDateTime,
                Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        orderCanonical.getOrderStatus().setCancellationCode(orderCancelCode);
        orderCanonical.getOrderStatus().setCancellationObservation(orderCancelObservation);

        return Mono.just(orderCanonical);

    }

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical,Long ecommerceId,
                                                            String source, String target, String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusOrder(
                ecommerceId, orderCanonical.getOrderStatus().getCode(), orderCanonical.getOrderStatus().getDetail(),
                localDateTime
        );

        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        return Mono.just(orderCanonical);

    }

    protected OrderCanonical getOrderFromIOrdersProjects(Long ecommerceId) {

        return getOrderFromIOrdersProjects(orderTransaction.getOrderByecommerceId(ecommerceId));

    }

    protected OrderCanonical getOrderFromIOrdersProjects(IOrderFulfillment iOrderFulfillment) {

        return objectToMapper
                .getOrderFromIOrdersProjects(
                        iOrderFulfillment,
                        orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId())
                );

    }

    protected List<IOrderFulfillment> getOrderLightByecommercesIds(Set<Long> ecommerceIds) {
        return orderTransaction.getOrderLightByecommercesIds(ecommerceIds);
    }

    protected void updateOrderOnlinePaymentStatusByExternalId(Long orderId, String onlinePaymentStatus) {
        orderTransaction.updateOrderOnlinePaymentStatusByExternalId(orderId,onlinePaymentStatus);
    }

    protected Mono<Boolean> processSendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment) {

        INotificationAdapter iNotificationAdapter = (actionDtoParam, iOrderFulfillmentParam) -> Mono.just(Boolean.TRUE);

        return iNotificationAdapter.sendNotification(actionDto,iOrderFulfillment);

    }
}
