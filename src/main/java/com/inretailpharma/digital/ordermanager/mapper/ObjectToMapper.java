package com.inretailpharma.digital.ordermanager.mapper;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.OrderStatusCanonical;
import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.ordermanager.util.Constant;
import com.inretailpharma.digital.ordermanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class ObjectToMapper {

    public OrderFulfillment convertOrderdtoToOrderEntity(OrderDto orderDto){
        log.info("[START] map-convertOrderdtoToOrderEntity");
        OrderFulfillment orderFulfillment = new OrderFulfillment();
        orderFulfillment.setSource(orderDto.getSource());
        orderFulfillment.setEcommercePurchaseId(orderDto.getEcommercePurchaseId());
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
        receiptType.setDocumentNumber(orderDto.getReceipt().getDocumentNumber());
        receiptType.setRuc(orderDto.getReceipt().getRuc());
        receiptType.setCompanyName(orderDto.getReceipt().getCompanyName());
        receiptType.setReceiptNote(orderDto.getReceipt().getNote());
        orderFulfillment.setReceiptType(receiptType);


        log.info("[END] map-convertOrderdtoToOrderEntity:{}",orderFulfillment);

        return orderFulfillment;

    }

    public OrderFulfillmentCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        OrderFulfillmentCanonical orderFulfillmentCanonical = new OrderFulfillmentCanonical();
        orderFulfillmentCanonical.setTrackerCode(iOrderFulfillment.getOrderId());

        Constant.OrderStatus orderStatus = Constant
                .OrderStatus.getByCode(iOrderFulfillment.getStatus());

        OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
        orderStatusCanonical.setStatusCode(orderStatus.getCode());
        orderStatusCanonical.setStatus(orderStatus.name());

        orderFulfillmentCanonical.setOrderStatus(orderStatusCanonical);
        orderFulfillmentCanonical.setLeadTime(DateUtils.getLocalDateTimeWithFormat(iOrderFulfillment.getLeadTime()));
        orderFulfillmentCanonical.setLocal(iOrderFulfillment.getLocalCode());
        orderFulfillmentCanonical.setCompany(iOrderFulfillment.getCompany());
        orderFulfillmentCanonical.setDocumentNumber(iOrderFulfillment.getDocumentNumber());
        orderFulfillmentCanonical.setTotalAmount(iOrderFulfillment.getTotalAmount());

        return orderFulfillmentCanonical;
    }
}
