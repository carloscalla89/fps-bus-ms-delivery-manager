package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
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

        if (Optional.ofNullable(orderDto.getSchedules()).isPresent()) {
            orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getCreatedOrder()));
            orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getScheduledTime()));
        } else {
            orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getCreatedOrder()));
            orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getScheduledTime()));
        }


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

        // object client
        Client client = new Client();
        client.setAnonimous(orderDto.getClient().getAnonimous());
        Optional.ofNullable(orderDto.getClient().getBirthDate())
                .ifPresent(r -> client.setBirthDate(DateUtils.getLocalDateFromStringDate(r)));
        client.setEmail(orderDto.getClient().getEmail());
        client.setDocumentNumber(orderDto.getClient().getDocumentNumber());
        client.setFirstName(orderDto.getClient().getFirstName());
        client.setLastName(orderDto.getClient().getLastName());
        client.setPhone(orderDto.getClient().getPhone());
        client.setInkaclub(orderDto.getClient().getHasInkaClub());
        orderFulfillment.setClient(client);


        // object address
        Address address = new Address();
        address.setName(orderDto.getAddress().getName());
        address.setNumber(orderDto.getAddress().getNumber());
        address.setApartment(orderDto.getAddress().getApartment());
        address.setCity(orderDto.getAddress().getCity());
        address.setDistrict(orderDto.getAddress().getDistrict());
        address.setProvince(orderDto.getAddress().getProvince());
        address.setDepartment(orderDto.getAddress().getDepartment());
        address.setCountry(orderDto.getAddress().getCountry());
        address.setNotes(orderDto.getAddress().getNotes());
        address.setLatitude(orderDto.getAddress().getLatitude());
        address.setLongitude(orderDto.getAddress().getLongitude());
        orderFulfillment.setAddress(address);

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



    public OrderCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        OrderCanonical orderCanonical = new OrderCanonical();

        Optional.ofNullable(iOrderFulfillment).ifPresent(s -> {
            orderCanonical.setId(s.getOrderId());
            orderCanonical.setTrackerId(s.getOrderId());
            orderCanonical.setExternalId(s.getExternalId());
            orderCanonical.setBridgePurchaseId(s.getBridgePurchaseId());
            orderCanonical.setDeliveryCost(s.getDeliveryCost());
            //orderCanonical.setDiscountApplied(s.);
            //orderCanonical.setSubTotalCost(s.);
            orderCanonical.setTotalAmount(s.getTotalCost());

            ClientCanonical client = new ClientCanonical();
            client.setFullName(Optional.ofNullable(s.getLastName()).orElse("")
                    + StringUtils.SPACE + Optional.ofNullable(s.getFirstName()).orElse(""));

            // ServiceType canonical
            OrderDetailCanonical orderDetailCanonical = new OrderDetailCanonical();


        });

        return orderCanonical;
    }

    public Mono<OrderCanonical> convertEntityToOrderCanonical(OrderDto orderDto) {
        log.info("[START] convertEntityToOrderCanonical");

        OrderCanonical orderCanonical = new OrderCanonical();

        // set ecommerce(shoppingcart) id
        orderCanonical.setEcommerceId(orderDto.getEcommercePurchaseId());

        // Set insink id
        orderCanonical.setExternalId(orderDto.getExternalPurchaseId());

        // Set localCode
        orderCanonical.setLocalCode(orderDto.getLocalCode());

        // set total amount
        orderCanonical.setDeliveryCost(orderDto.getDeliveryCost());
        orderCanonical.setDiscountApplied(orderDto.getDiscountApplied());
        orderCanonical.setSubTotalCost(orderDto.getSubTotalCost());
        orderCanonical.setTotalAmount(orderDto.getTotalCost());

        // set client
        ClientCanonical client = new ClientCanonical();
        client.setFullName(
                Optional.ofNullable(orderDto.getClient().getLastName()).orElse(StringUtils.EMPTY)
                + StringUtils.SPACE
                + Optional.ofNullable(orderDto.getClient().getFirstName()).orElse(StringUtils.EMPTY)
        );
        client.setAnonimous(orderDto.getClient().getAnonimous());
        client.setBirthDate(orderDto.getClient().getBirthDate());
        client.setDocumentNumber(orderDto.getClient().getDocumentNumber());
        client.setEmail(orderDto.getClient().getEmail());
        client.setPhone(orderDto.getClient().getPhone());
        orderCanonical.setClient(client);

        // set Address
        AddressCanonical address = new AddressCanonical();
        address.setName(

                Optional.ofNullable(orderDto.getAddress().getName()).orElse(StringUtils.EMPTY)
                        + StringUtils.SPACE
                + Optional.ofNullable(orderDto.getAddress().getStreet()).orElse(StringUtils.EMPTY)
                        + StringUtils.SPACE
                + Optional.ofNullable(orderDto.getAddress().getNumber()).orElse(StringUtils.EMPTY)
        );
        address.setDistrict(orderDto.getAddress().getDistrict());
        address.setDepartment(orderDto.getAddress().getDepartment());
        address.setCountry(orderDto.getAddress().getCountry());
        orderCanonical.setAddress(address);

        // set items
        orderCanonical.setOrderItems(
                orderDto.getOrderItem().stream().map(r -> {
                    OrderItemCanonical itemCanonical = new OrderItemCanonical();
                    itemCanonical.setProductCode(r.getProductCode());
                    itemCanonical.setProductName(r.getProductName());
                    itemCanonical.setShortDescription(r.getShortDescription());
                    itemCanonical.setBrand(r.getBrand());
                    itemCanonical.setQuantity(r.getQuantity());
                    itemCanonical.setUnitPrice(r.getUnitPrice());
                    itemCanonical.setTotalPrice(r.getTotalPrice());
                    itemCanonical.setFractionated(r.getFractionated());

                    return itemCanonical;
                }).collect(Collectors.toList())
        );

        // set detail order
        OrderDetailCanonical orderDetailCanonical = new OrderDetailCanonical();

        Optional.ofNullable(orderDto.getSchedules()).ifPresent(r -> {
            orderDetailCanonical.setConfirmedSchedule(r.getScheduledTime());
            orderDetailCanonical.setCreatedOrder(r.getCreatedOrder());
            orderDetailCanonical.setStartHour(r.getStartHour());
            orderDetailCanonical.setEndHour(r.getEndHour());
            orderDetailCanonical.setLeadTime(r.getLeadTime());
        });

        if (orderDto.getCreatedOrder() != null && orderDto.getScheduledTime() != null) {
            orderDetailCanonical.setConfirmedSchedule(orderDto.getScheduledTime());
            orderDetailCanonical.setCreatedOrder(orderDto.getScheduledTime());
        }

        orderCanonical.setOrderDetail(orderDetailCanonical);

        // set Receipt
        ReceiptCanonical receipt = new ReceiptCanonical();
        receipt.setAddress(orderDto.getReceipt().getCompanyAddress());
        receipt.setCompanyName(orderDto.getReceipt().getCompanyName());
        receipt.setRuc(orderDto.getReceipt().getRuc());
        orderCanonical.setReceipt(receipt);

        // set Payment
        PaymentMethodCanonical paymentMethod = new PaymentMethodCanonical();
        paymentMethod.setCardProvider(orderDto.getPayment().getCardProvider());
        paymentMethod.setChangeAmount(orderDto.getPayment().getChangeAmount());
        paymentMethod.setPaidAmount(orderDto.getPayment().getPaidAmount());
        orderCanonical.setPaymentMethod(paymentMethod);
        log.info("[END] convertEntityToOrderCanonical:{}",orderCanonical);

        return Mono.just(orderCanonical);

    }

    public List<OrderCancellationCanonical> convertEntityOrderCancellationToCanonical(
            List<CancellationCodeReason> cancelReasons) {

        return cancelReasons.stream().map(r -> {
            OrderCancellationCanonical orderCancellationCanonical = new OrderCancellationCanonical();
            orderCancellationCanonical.setCode(r.getCode());
            orderCancellationCanonical.setType(r.getType());
            orderCancellationCanonical.setDescription(r.getReason());

            return orderCancellationCanonical;
        }).collect(Collectors.toList());

    }

}
