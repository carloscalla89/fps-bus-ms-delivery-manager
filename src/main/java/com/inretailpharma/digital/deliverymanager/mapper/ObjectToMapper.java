package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
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

        Optional.ofNullable(orderDto.getAddress()).ifPresent(r -> {
            address.setName(r.getName());
            address.setNumber(r.getNumber());
            address.setApartment(r.getApartment());
            address.setCity(r.getCity());
            address.setDistrict(r.getDistrict());
            address.setProvince(r.getProvince());
            address.setDepartment(r.getDepartment());
            address.setCountry(r.getCountry());
            address.setNotes(r.getNotes());
            address.setLatitude(r.getLatitude());
            address.setLongitude(r.getLongitude()); 
            address.setStreet(r.getStreet());
        });

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

        log.info("[END] map-convertOrderdtoToOrderEntity");

        return orderFulfillment;

    }

    public OrderCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        log.debug("[START] map-convertIOrderDtoToOrderFulfillmentCanonical");
    	
        OrderCanonical orderCanonical = new OrderCanonical();
        Optional.ofNullable(iOrderFulfillment).ifPresent(o -> {
        	
        	orderCanonical.setEcommerceId(o.getEcommerceId());
        	orderCanonical.setExternalId(o.getExternalId());
        	orderCanonical.setBridgePurchaseId(o.getBridgePurchaseId());
        	
        	orderCanonical.setTotalAmount(o.getTotalCost());
        	orderCanonical.setDeliveryCost(o.getDeliveryCost());
        	
        	orderCanonical.setCompany(o.getCompanyName());
        	orderCanonical.setLocalCode(o.getCenterCode());
        	orderCanonical.setLocal(o.getCenterName());
        	
        	ClientCanonical client = new ClientCanonical();
        	client.setDocumentNumber(o.getDocumentNumber());
        	client.setFullName(Optional.ofNullable(o.getLastName()).orElse(StringUtils.EMPTY)
                    + StringUtils.SPACE + Optional.ofNullable(o.getFirstName()).orElse(StringUtils.EMPTY));
        	client.setEmail(o.getEmail());
        	client.setPhone(o.getPhone());
        	client.setHasInkaClub(o.getInkaClub());        	

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();
            Optional.ofNullable(o.getScheduledTime()).ifPresent(date -> {
            	orderDetail.setConfirmedSchedule(DateUtils.getLocalDateTimeWithFormat(date));
            });          
            orderDetail.setLeadTime(o.getLeadTime());
            orderDetail.setServiceCode(o.getServiceTypeCode());
            orderDetail.setServiceName(o.getServiceTypeName());
            
            AddressCanonical address = new AddressCanonical();
            address.setName(
                    Optional.ofNullable(o.getStreet()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(o.getNumber()).orElse(StringUtils.EMPTY)
            );
            address.setDepartment(o.getDepartment());
            address.setProvince(o.getProvince());
            address.setDistrict(o.getDistrict());
            address.setLatitude(o.getLatitude());
            address.setLongitude(o.getLongitude());
            address.setNotes(o.getNotes());
            
            ReceiptCanonical receipt = new ReceiptCanonical();
            receipt.setType(o.getReceiptType());
            receipt.setCompanyName(o.getCompanyNameReceipt());
            receipt.setAddress(o.getCompanyAddressReceipt());
            receipt.setRuc(o.getRuc());
            receipt.setNote(o.getNoteReceipt());
            
            PaymentMethodCanonical paymentMethod = new PaymentMethodCanonical();
            paymentMethod.setType(o.getPaymentType());
            paymentMethod.setCardProvider(o.getCardProvider());
            paymentMethod.setPaidAmount(o.getPaidAmount());
            paymentMethod.setChangeAmount(o.getChangeAmount()); 

            orderCanonical.setClient(client);
            orderCanonical.setOrderDetail(orderDetail);
            orderCanonical.setAddress(address);
            orderCanonical.setReceipt(receipt);
            orderCanonical.setPaymentMethod(paymentMethod);
        });

        log.debug("[END] map-convertIOrderDtoToOrderFulfillmentCanonical:{}", orderCanonical);
        return orderCanonical;
    }
    
    public OrderItemCanonical convertIOrderItemDtoToOrderItemFulfillmentCanonical(IOrderItemFulfillment iOrderItemFulfillment) {
    	log.debug("[START] map-convertIOrderItemDtoToOrderItemFulfillmentCanonical");
    	
    	OrderItemCanonical orderItemCanonical = new OrderItemCanonical();
    	Optional.ofNullable(iOrderItemFulfillment).ifPresent(o -> {
    		orderItemCanonical.setProductCode(o.getProductCode());
    		orderItemCanonical.setProductName(o.getNameProduct());
    		orderItemCanonical.setShortDescription(o.getShortDescriptionProduct());
    		orderItemCanonical.setBrand(o.getBrandProduct());
    		orderItemCanonical.setQuantity(o.getQuantity());
    		orderItemCanonical.setUnitPrice(o.getUnitPrice());
    		orderItemCanonical.setTotalPrice(o.getTotalPrice());
    		orderItemCanonical.setFractionated(Constant.Logical.Y.name().equals(o.getFractionated()));
    	});
    	
    	log.debug("[END] map-convertIOrderItemDtoToOrderItemFulfillmentCanonical:{}", orderItemCanonical);
        return orderItemCanonical;    	
    }

    public Mono<OrderCanonical> convertEntityToOrderCanonical(OrderDto orderDto) {
        log.info("[START] convertEntityToOrderCanonical");

        OrderCanonical orderCanonical = new OrderCanonical();

        // set ecommerce(shoppingcart) id
        orderCanonical.setEcommerceId(orderDto.getEcommercePurchaseId());

        // Set insink id
        orderCanonical.setExternalId(orderDto.getExternalPurchaseId());

        // Set bridge purchase id(online payment id)
        orderCanonical.setBridgePurchaseId(orderDto.getBridgePurchaseId());

        // Set localCode
        orderCanonical.setLocalCode(orderDto.getLocalCode());

        // set total amount
        orderCanonical.setDeliveryCost(orderDto.getDeliveryCost());
        orderCanonical.setDiscountApplied(orderDto.getDiscountApplied());
        orderCanonical.setSubTotalCost(orderDto.getSubTotalCost());
        orderCanonical.setTotalAmount(orderDto.getTotalCost());

        // set client
        ClientCanonical client = new ClientCanonical();

        Optional.ofNullable(orderDto.getClient()).ifPresent(r -> {
            client.setFullName(
                    Optional.ofNullable(r.getLastName()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(r.getFirstName()).orElse(StringUtils.EMPTY)
            );
            client.setAnonimous(r.getAnonimous());
            client.setBirthDate(r.getBirthDate());
            client.setDocumentNumber(r.getDocumentNumber());
            client.setEmail(r.getEmail());
            client.setPhone(r.getPhone());
        });

        orderCanonical.setClient(client);

        // set Address
        AddressCanonical address = new AddressCanonical();

        Optional.ofNullable(orderDto.getAddress()).ifPresent(r -> {
            address.setName(
                    Optional.ofNullable(r.getName()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(r.getStreet()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(r.getNumber()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(r.getCity()).orElse(StringUtils.EMPTY)
            );
            address.setDistrict(r.getDistrict());
            address.setDepartment(r.getDepartment());
            address.setCountry(r.getCountry());
        });

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
        OrderDetailCanonical orderDetail = new OrderDetailCanonical();

        Optional.ofNullable(orderDto.getSchedules()).ifPresent(r -> {
            orderDetail.setConfirmedSchedule(r.getScheduledTime());
            orderDetail.setCreatedOrder(r.getCreatedOrder());
            orderDetail.setStartHour(r.getStartHour());
            orderDetail.setEndHour(r.getEndHour());
            orderDetail.setLeadTime(r.getLeadTime());
        });

        if (orderDto.getCreatedOrder() != null && orderDto.getScheduledTime() != null) {
            orderDetail.setConfirmedSchedule(orderDto.getScheduledTime());
            orderDetail.setCreatedOrder(orderDto.getCreatedOrder());
        }

        orderCanonical.setOrderDetail(orderDetail);

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
        log.info("[END] convertEntityToOrderCanonical");

        return Mono.just(orderCanonical);

    }

    public List<CancellationCanonical> convertEntityOrderCancellationToCanonical(
            List<CancellationCodeReason> cancelReasons) {

        return cancelReasons.stream().map(r -> {
            CancellationCanonical cancellationCanonical = new CancellationCanonical();
            cancellationCanonical.setCode(r.getCode());
            cancellationCanonical.setType(r.getType());
            cancellationCanonical.setDescription(r.getReason());

            return cancellationCanonical;
        }).collect(Collectors.toList());

    }

}
