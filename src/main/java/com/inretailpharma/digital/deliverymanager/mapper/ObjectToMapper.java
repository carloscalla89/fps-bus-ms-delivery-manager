package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.*;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ObjectToMapper {

    public OrderFulfillment convertOrderdtoToOrderEntity(OrderDto orderDto){
        log.info("[START] map-convertOrderdtoToOrderEntity");
        OrderFulfillment orderFulfillment = new OrderFulfillment();
        orderFulfillment.setSource(orderDto.getSource());
        orderFulfillment.setEcommercePurchaseId(orderDto.getEcommercePurchaseId());
        orderFulfillment.setTrackerId(orderDto.getTrackerId());
        orderFulfillment.setExternalPurchaseId(orderDto.getExternalPurchaseId());
        orderFulfillment.setBridgePurchaseId(orderDto.getBridgePurchaseId());
        orderFulfillment.setTotalCost(orderDto.getTotalCost());
        orderFulfillment.setDeliveryCost(orderDto.getDeliveryCost());
        orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getCreatedOrder()));
        orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getScheduledTime()));
        orderFulfillment.setDocumentNumber(orderDto.getClient().getDocumentNumber());

        // object orderItem
        orderFulfillment.setOrderItem(
                orderDto.getOrderItem().stream().map(r -> {
                    OrderFulfillmentItem orderFulfillmentItem = new OrderFulfillmentItem();
                    orderFulfillmentItem.setProductCode(r.getProductCode());
                    orderFulfillmentItem.setProductName(r.getProductName());
                    orderFulfillmentItem.setShortDescription(r.getShortDescription());
                    orderFulfillmentItem.setBrand(r.getBrand());
                    orderFulfillmentItem.setQuantity(r.getQuantity());
                    orderFulfillmentItem.setUnitPrice(r.getUnitPrice());
                    orderFulfillmentItem.setTotalPrice(r.getTotalPrice());
                    orderFulfillmentItem.setFractionated(Constant.Logical.parse(r.getFractionated()));

                    return orderFulfillmentItem;
                }).collect(Collectors.toList())
        );

        // object payment_method
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setPaymentType(
                PaymentMethod
                        .PaymentType
                        .getPaymentTypeByNameType(orderDto.getPayment().getType())
        );
        paymentMethod.setCardProvider(orderDto.getPayment().getCardProvider());
        paymentMethod.setPaidAmount(orderDto.getPayment().getPaidAmount());
        paymentMethod.setChangeAmount(orderDto.getPayment().getChangeAmount());
        orderFulfillment.setPaymentMethod(paymentMethod);

        // object receipt
        ReceiptType receiptType = new ReceiptType();
        receiptType.setName(orderDto.getReceipt().getName());
        receiptType.setDocumentNumber(orderDto.getClient().getDocumentNumber());
        receiptType.setRuc(orderDto.getReceipt().getRuc());
        receiptType.setCompanyName(orderDto.getReceipt().getCompanyName());
        receiptType.setReceiptNote(orderDto.getReceipt().getNote());
        orderFulfillment.setReceiptType(receiptType);

        log.info("[END] map-convertOrderdtoToOrderEntity:{}",orderFulfillment);

        return orderFulfillment;

    }

    public OrderFulfillmentCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        OrderFulfillmentCanonical orderFulfillmentCanonical = new OrderFulfillmentCanonical();

        Optional.ofNullable(iOrderFulfillment).ifPresent(s -> {
            orderFulfillmentCanonical.setId(s.getOrderId());
            orderFulfillmentCanonical.setTrackerId(s.getTrackerId());
            orderFulfillmentCanonical.setExternalId(s.getExternalId());

            Optional.ofNullable(s.getStatus()).ifPresent(r -> {
                Constant.OrderStatus orderStatus = Constant.OrderStatus.getByCode(r);

                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setStatusCode(orderStatus.getCode());
                orderStatusCanonical.setStatus(orderStatus.name());

                orderFulfillmentCanonical.setOrderStatus(orderStatusCanonical);
            });


            Optional
                    .ofNullable(s.getLeadTime())
                    .ifPresent(r -> orderFulfillmentCanonical.setLeadTime(DateUtils.getLocalDateTimeWithFormat(r)));

            // ServiceType canonical
            ServiceTypeCanonical serviceTypeCanonical = new ServiceTypeCanonical();
            serviceTypeCanonical.setCode(s.getServiceTypeCode());
            serviceTypeCanonical.setName(s.getServiceTypeName());
            orderFulfillmentCanonical.setServiceType(serviceTypeCanonical);

            orderFulfillmentCanonical.setLocal(s.getLocalCode());
            orderFulfillmentCanonical.setCompany(s.getCompany());
            orderFulfillmentCanonical.setDocumentNumber(s.getDocumentNumber());
            orderFulfillmentCanonical.setTotalAmount(s.getTotalAmount());

            orderFulfillmentCanonical.setAttempt(s.getAttempt());
            orderFulfillmentCanonical.setAttemptTracker(s.getAttemptTracker());
        });

        return orderFulfillmentCanonical;
    }

    public OrderFulfillmentCanonical convertEntityToOrderFulfillmentCanonical(ServiceLocalOrder serviceLocalOrderEntity) {

        OrderFulfillment orderFulfillment = serviceLocalOrderEntity.getServiceLocalOrderIdentity().getOrderFulfillment();
        OrderFulfillmentCanonical orderFulfillmentCanonical = new OrderFulfillmentCanonical();

        // set id
        orderFulfillmentCanonical.setId(orderFulfillment.getId());

        // set ecommerce(shoppingcart) id
        orderFulfillmentCanonical.setEcommerceId(orderFulfillment.getEcommercePurchaseId());

        // set tracker id
        orderFulfillmentCanonical.setTrackerId(orderFulfillment.getTrackerId());

        // Set insink id
        orderFulfillmentCanonical.setExternalId(orderFulfillment.getExternalPurchaseId());

        // set status
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setStatusCode(
                serviceLocalOrderEntity.getServiceLocalOrderIdentity().getOrderStatus().getCode()
        );
        orderStatus.setStatus(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getOrderStatus().getType());
        orderStatus.setStatusDetail(serviceLocalOrderEntity.getStatusDetail());
        orderFulfillmentCanonical.setOrderStatus(orderStatus);

        // Set type and name of service
        ServiceTypeCanonical serviceType = new ServiceTypeCanonical();
        serviceType.setCode(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getServiceType().getCode());
        serviceType.setName(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getServiceType().getName());
        orderFulfillmentCanonical.setServiceType(serviceType);

        // localCode, locals and the company
        orderFulfillmentCanonical.setLocalCode(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getLocal().getCode());
        orderFulfillmentCanonical.setLocal(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getLocal().getName());
        orderFulfillmentCanonical.setCompany(serviceLocalOrderEntity.getServiceLocalOrderIdentity().getLocal().getCompany().getName());

        orderFulfillmentCanonical.setTotalAmount(orderFulfillment.getTotalCost());
        orderFulfillmentCanonical.setLeadTime(DateUtils.getLocalDateTimeWithFormat(orderFulfillment.getScheduledTime()));
        orderFulfillmentCanonical.setDocumentNumber(orderFulfillment.getDocumentNumber());


        // attempt of insink and attemptTracker of tracker
        orderFulfillmentCanonical.setAttempt(serviceLocalOrderEntity.getAttempt());
        orderFulfillmentCanonical.setAttemptTracker(serviceLocalOrderEntity.getAttemptTracker());


        // Payment method canonical
        PaymentMethodCanonical paymentMethodCanonical = new PaymentMethodCanonical();
        paymentMethodCanonical.setType(orderFulfillment.getPaymentMethod().getPaymentType().name());
        paymentMethodCanonical.setProviderCard(orderFulfillment.getPaymentMethod().getCardProvider());
        orderFulfillmentCanonical.setPaymentMethod(paymentMethodCanonical);

        ReceiptCanonical receiptCanonical = new ReceiptCanonical();
        receiptCanonical.setType(orderFulfillment.getReceiptType().getName());
        receiptCanonical.setRuc(orderFulfillment.getReceiptType().getRuc());
        receiptCanonical.setCompanyName(orderFulfillment.getReceiptType().getCompanyName());
        receiptCanonical.setAddress(orderFulfillment.getReceiptType().getCompanyAddress());
        orderFulfillmentCanonical.setReceipt(receiptCanonical);

        log.info("Map result orderFulfillmentCanonical:{}",orderFulfillmentCanonical);

        return orderFulfillmentCanonical;

    }


}
