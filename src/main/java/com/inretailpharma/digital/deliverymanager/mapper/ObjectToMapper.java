package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.*;

import com.inretailpharma.digital.deliverymanager.canonical.integration.ProductCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.AddressCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ClientCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.PaymentMethodCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ReceiptCanonical;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.AddressInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ClientInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.DrugstoreCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.*;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.Optional;

import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ObjectToMapper {

    public OrderInkatrackerCanonical convertOrderToOrderInkatrackerCanonical(OrderCanonical orderCanonical) {

        OrderInkatrackerCanonical orderInkatrackerCanonical = new OrderInkatrackerCanonical();
        orderInkatrackerCanonical.setOrderExternalId(orderCanonical.getEcommerceId());
        orderInkatrackerCanonical.setDateCreated(
                DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getCreatedOrder()).toEpochSecond(ZoneOffset.UTC)
        );
        orderInkatrackerCanonical.setSource(orderCanonical.getSource());
        Optional.ofNullable(orderCanonical.getDiscountApplied())
                .ifPresent(r -> orderInkatrackerCanonical.setDiscountApplied(r.doubleValue()));
        orderInkatrackerCanonical.setAddress(getFromtOrderCanonical(orderCanonical.getAddress(), orderCanonical.getOrderDetail().getLeadTime()));
        orderInkatrackerCanonical.setClient(getFromtOrderCanonical(orderCanonical.getClient()));
        orderInkatrackerCanonical.setDeliveryCost(
                Optional.ofNullable(orderCanonical.getDeliveryCost())
                        .map(BigDecimal::doubleValue)
                        .orElse(null));
        orderInkatrackerCanonical.setDeliveryService(Constant.TrackerImplementation.getByCode(orderCanonical.getOrderDetail().getServiceCode()).getId());
        // para obtener la info del drugstore, se llamará al servicio de fulfillment-center
        orderInkatrackerCanonical.setMaxDeliveryTime(
                DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                         .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())
                         .toEpochSecond(ZoneOffset.UTC)
        );
        orderInkatrackerCanonical.setOrderItems(createFirebaseOrderItemsFromOrderItemCanonical(orderCanonical.getOrderItems()));
        orderInkatrackerCanonical.setOrderStatus(getFromOrderCanonical(orderCanonical));
        orderInkatrackerCanonical.setTotalCost(orderCanonical.getTotalAmount().doubleValue());
        orderInkatrackerCanonical.setPaymentMethod(getPaymentMethodFromOrderCanonical(orderCanonical));

        PreviousStatusCanonical previousStatus = new PreviousStatusCanonical();
        previousStatus.setDate(orderInkatrackerCanonical.getDateCreated());
        previousStatus.setOrderStatus(orderInkatrackerCanonical.getOrderStatus().getStatusName());
        orderInkatrackerCanonical.setPreviousStatus(Collections.singletonList(previousStatus));
        orderInkatrackerCanonical.setInkaDeliveryId(orderCanonical.getExternalId());
        orderInkatrackerCanonical.setReceipt(getReceiptFromOrderCanonical(orderCanonical));
        ScheduledCanonical scheduledCanonical = new ScheduledCanonical();
        scheduledCanonical.setStartDate(
                DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule()).toEpochSecond(ZoneOffset.UTC)
        );
        scheduledCanonical.setEndDate(
                DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                        .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())
                        .toEpochSecond(ZoneOffset.UTC)
        );
        orderInkatrackerCanonical.setScheduled(scheduledCanonical);

        DrugstoreCanonical drugstoreCanonical = new DrugstoreCanonical();
        drugstoreCanonical.setId(orderCanonical.getId());
        drugstoreCanonical.setName(orderCanonical.getLocal());
        drugstoreCanonical.setDescription(orderCanonical.getLocalDescription());
        drugstoreCanonical.setAddress(orderCanonical.getLocalAddress());
        drugstoreCanonical.setLatitude(
                Optional.ofNullable(orderCanonical.getLocalLatitude())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0)
        );
        drugstoreCanonical.setLongitude(
                Optional.ofNullable(orderCanonical.getLocalLongitude())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0)
        );

        return orderInkatrackerCanonical;
    }

    private ReceiptInkatrackerCanonical getReceiptFromOrderCanonical(OrderCanonical orderCanonical) {
        ReceiptInkatrackerCanonical receiptInkatrackerCanonical = new ReceiptInkatrackerCanonical();
        receiptInkatrackerCanonical.setType(orderCanonical.getReceipt().getType());
        receiptInkatrackerCanonical.setCompanyId(orderCanonical.getReceipt().getRuc());
        receiptInkatrackerCanonical.setCompanyName(orderCanonical.getReceipt().getCompanyName());
        receiptInkatrackerCanonical.setCompanyAddress(orderCanonical.getReceipt().getAddress());
        receiptInkatrackerCanonical.setNote(
                orderCanonical.getReceipt().getType()
                        + Optional.ofNullable(orderCanonical.getReceipt().getType())
                                  .filter(r -> r.equalsIgnoreCase(Constant.Receipt.INVOICE))
                                  .map(r -> " - " + orderCanonical.getReceipt().getRuc())
                                  .orElse(""));
        return receiptInkatrackerCanonical;
    }

    private PaymentMethodInkatrackerCanonical getPaymentMethodFromOrderCanonical(OrderCanonical orderCanonical) {
        PaymentMethodInkatrackerCanonical canonical = new PaymentMethodInkatrackerCanonical();
        canonical.setType(orderCanonical.getPaymentMethod().getType());
        canonical.setNote(orderCanonical.getPaymentMethod().getNote());
        canonical.setPaidAmount(
                Optional.ofNullable(orderCanonical.getPaymentMethod().getPaidAmount())
                        .map(BigDecimal::doubleValue)
                        .orElse(null)
        );
        canonical.setChangeAmount(
                Optional.ofNullable(orderCanonical.getPaymentMethod().getChangeAmount())
                        .map(BigDecimal::doubleValue)
                        .orElse(null)
        );
        canonical.setProvider(orderCanonical.getPaymentMethod().getCardProvider());

        return canonical;
    }

    private OrderStatusInkatrackerCanonical getFromOrderCanonical(OrderCanonical orderCanonical) {
        OrderStatusInkatrackerCanonical orderStatusInkatrackerCanonical = new OrderStatusInkatrackerCanonical();
        orderStatusInkatrackerCanonical.setStatusName(Constant.OrderStatus.getByCode(orderCanonical.getOrderStatus().getCode()).getStatusTracker());
        orderStatusInkatrackerCanonical.setStatusDate(
                DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedOrder()).toEpochSecond(ZoneOffset.UTC)
        );
        return orderStatusInkatrackerCanonical;
    }

    private List<OrderItemInkatrackerCanonical> createFirebaseOrderItemsFromOrderItemCanonical(List<OrderItemCanonical> itemCanonicals) {

        List<OrderItemInkatrackerCanonical> itemCanonicalList = new ArrayList<>();


        for (OrderItemCanonical itemCanonical : itemCanonicals) {
            OrderItemInkatrackerCanonical canonical = new OrderItemInkatrackerCanonical();
            canonical.setBrand(itemCanonical.getBrand());
            canonical.setFractionated(Optional.ofNullable(itemCanonical.getFractionated()).map(r -> r?"1":"0").orElse("0"));
            canonical.setName(itemCanonical.getProductName());
            canonical.setQuantity(itemCanonical.getQuantity());
            canonical.setSku(itemCanonical.getProductCode());
            canonical.setEanCode(itemCanonical.getProductEan());
            canonical.setTotalPrice(itemCanonical.getTotalPrice().doubleValue());
            canonical.setUnitPrice(itemCanonical.getUnitPrice().doubleValue());
            canonical.setWithStock(Constant.Logical.Y.name());
            canonical.setPresentationId(itemCanonical.getPresentationId());
            canonical.setPresentationDescription(itemCanonical.getPresentationDescription());
            canonical.setQuantityUnits(itemCanonical.getQuantityUnits());
            canonical.setQuantityPresentation(itemCanonical.getQuantityPresentation());

            itemCanonicalList.add(canonical);
        }
        return itemCanonicalList;

    }

    private AddressInkatrackerCanonical getFromtOrderCanonical(AddressCanonical addressCanonical, Integer deliveryTime) {
        AddressInkatrackerCanonical addressInkatrackerCanonical = new AddressInkatrackerCanonical();
        addressInkatrackerCanonical.setName(addressCanonical.getName());
        addressInkatrackerCanonical.setLatitude(
                Optional.ofNullable(addressCanonical.getLatitude())
                        .orElse((BigDecimal.ZERO)).doubleValue());
        addressInkatrackerCanonical.setLongitude(
                Optional.ofNullable(addressCanonical.getLongitude())
                        .orElse((BigDecimal.ZERO)).doubleValue());
        addressInkatrackerCanonical.setCity(addressCanonical.getNameAddress());
        addressInkatrackerCanonical.setDistrict(addressCanonical.getDistrict());
        addressInkatrackerCanonical.setStreet(addressCanonical.getStreet());
        addressInkatrackerCanonical.setNumber(addressCanonical.getNumber());
        addressInkatrackerCanonical.setApartment(addressCanonical.getApartment());
        addressInkatrackerCanonical.setNotes(addressCanonical.getNotes());
        addressInkatrackerCanonical.setZoneEta(deliveryTime);

        return addressInkatrackerCanonical;
    }

    private ClientInkatrackerCanonical getFromtOrderCanonical(ClientCanonical clientCanonical) {
        ClientInkatrackerCanonical clientInkatrackerCanonical = new ClientInkatrackerCanonical();
        Optional.ofNullable(clientCanonical.getBirthDate()).ifPresent(r ->
                clientInkatrackerCanonical.setBirthDate(DateUtils.getLocalDateFromStringDate(r).toEpochDay()));
        clientInkatrackerCanonical.setDni(clientCanonical.getDocumentNumber());
        clientInkatrackerCanonical.setEmail(clientCanonical.getEmail());
        clientInkatrackerCanonical.setFirstName(clientCanonical.getFirstName());
        clientInkatrackerCanonical.setLastName(clientCanonical.getLastName());
        clientInkatrackerCanonical.setPhone(clientCanonical.getPhone());
        clientInkatrackerCanonical.setIsAnonymous(
                Optional.ofNullable(clientCanonical.getAnonimous())
                        .orElse(0)==0?"N":"Y"
        );
        clientInkatrackerCanonical.setHasInkaClub(
                Optional.ofNullable(clientCanonical.getHasInkaClub())
                        .orElse(0)==0?"N":"Y"
        );
        clientInkatrackerCanonical.setUserId(clientCanonical.getUserId());
        clientInkatrackerCanonical.setNotificationToken(clientCanonical.getNotificationToken());
        return clientInkatrackerCanonical;
    }

    public OrderFulfillment convertOrderdtoToOrderEntity(OrderDto orderDto){
        log.info("[START] map-convertOrderdtoToOrderEntity");

        OrderFulfillment orderFulfillment = new OrderFulfillment();
        orderFulfillment.setSource(orderDto.getSource());
        orderFulfillment.setEcommercePurchaseId(orderDto.getEcommercePurchaseId());
        orderFulfillment.setTrackerId(orderDto.getTrackerId());
        orderFulfillment.setExternalPurchaseId(orderDto.getExternalPurchaseId());
        orderFulfillment.setBridgePurchaseId(orderDto.getBridgePurchaseId());
        orderFulfillment.setDiscountApplied(orderDto.getDiscountApplied());
        orderFulfillment.setSubTotalCost(orderDto.getSubTotalCost());
        orderFulfillment.setTotalCost(orderDto.getTotalCost());
        orderFulfillment.setDeliveryCost(orderDto.getDeliveryCost());
        orderFulfillment.setSourceCompanyName(orderDto.getSourceCompanyName());

        orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getCreatedOrder()));
        orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getScheduledTime()));
        orderFulfillment.setConfirmedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getConfirmedOrder()));

        // object orderItem
        orderFulfillment.setOrderItem(
                orderDto.getOrderItem().stream().map(r -> {
                    OrderFulfillmentItem orderFulfillmentItem = new OrderFulfillmentItem();
                    orderFulfillmentItem.setProductCode(r.getProductCode());
                    orderFulfillmentItem.setProductSapCode(r.getProductSapCode());
                    orderFulfillmentItem.setEanCode(r.getEanCode());
                    orderFulfillmentItem.setProductName(r.getProductName());
                    orderFulfillmentItem.setShortDescription(r.getShortDescription());
                    orderFulfillmentItem.setBrand(r.getBrand());
                    orderFulfillmentItem.setQuantity(r.getQuantity());
                    orderFulfillmentItem.setUnitPrice(r.getUnitPrice());
                    orderFulfillmentItem.setTotalPrice(r.getTotalPrice());
                    orderFulfillmentItem.setFractionated(Constant.Logical.parse(r.getFractionated()));
                    orderFulfillmentItem.setFractionalDiscount(r.getFractionalDiscount());
                    orderFulfillmentItem.setPresentationId(r.getPresentationId());
                    orderFulfillmentItem.setPresentationDescription(r.getPresentationDescription());
                    orderFulfillmentItem.setQuantityUnits(r.getQuantity());
                    orderFulfillmentItem.setQuantityPresentation(r.getQuantityPresentation());

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
        client.setNotificationToken(orderDto.getClient().getNotificationToken());
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
            address.setReceiver(r.getReceiver());
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
        orderFulfillment.setNotes(orderDto.getNotes());

        log.info("[END] map-convertOrderdtoToOrderEntity");

        return orderFulfillment;

    }

    public static void main(String[] args) {
        Long test_timestamp = 1595127600L;
        LocalDateTime triggerTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(test_timestamp),
                        TimeZone.getDefault().toZoneId());
        System.out.println(triggerTime);

    }
    public OrderInfoCanonical convertIOrderDtoToOrderTrackerFulfillmentCanonical(IOrderFulfillment iOrderFulfillment,
                                                                                 List<ProductCanonical> productList,
                                                                                 List<IOrderItemFulfillment> orderItemDtoList) {
        log.debug("[START] map-convertIOrderDtoToOrderTrackerFulfillmentCanonical");
        OrderInfoCanonical orderInfoCanonical = new OrderInfoCanonical();
        Optional.ofNullable(iOrderFulfillment).ifPresent(o -> {

            orderInfoCanonical.setOrderExternalId(o.getEcommerceId());
            orderInfoCanonical.setSource(o.getSource());
            orderInfoCanonical.setDateCreated(o.getCreatedOrder().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            orderInfoCanonical.setDiscountApplied(new BigDecimal(0));
            orderInfoCanonical.setSourceCompanyName(o.getSourceCompanyName());
            com.inretailpharma.digital.deliverymanager.canonical.inkatracker.AddressCanonical address
                    = new com.inretailpharma.digital.deliverymanager.canonical.inkatracker.AddressCanonical();
            address.setName(
                    Optional.ofNullable(o.getStreet()).orElse(StringUtils.EMPTY)
                            + StringUtils.SPACE
                            + Optional.ofNullable(o.getNumber()).orElse(StringUtils.EMPTY)
            );
            address.setDistrict(o.getDistrict());
            address.setLatitude(o.getLatitude().doubleValue());
            address.setLongitude(o.getLongitude().doubleValue());
            address.setNotes(o.getNotes());
            address.setApartment(o.getApartment());
            address.setStreet(o.getStreet());
            address.setCity(o.getCity());
            address.setCountry(o.getCountry());
            address.setDistrict(o.getDistrict());
            address.setNumber(o.getNumber());
            orderInfoCanonical.setAddress(address);

            com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ClientCanonical client
                    = new com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ClientCanonical();

            client.setDni(o.getDocumentNumber());
            client.setFirstName(o.getFirstName());
            client.setLastName(o.getLastName());
            client.setEmail(o.getEmail());
            client.setPhone(o.getPhone());
            client.setHasInkaClub(Constant.Logical.N.name());
            client.setIsAnonymous(Constant.Logical.Y.name());
            orderInfoCanonical.setClient(client);

            orderInfoCanonical.setDeliveryCost(o.getDeliveryCost().doubleValue());
            orderInfoCanonical.setDeliveryService(Constant.DS_INKATRACKER);

            Drugstore drugstore = new Drugstore();
            drugstore.setId(Constant.DEFAULT_DRUGSTORE_ID);
            orderInfoCanonical.setDrugstore(drugstore);

            orderInfoCanonical.setDrugstoreId(Constant.DEFAULT_DRUGSTORE_ID);
            orderInfoCanonical.setMaxDeliveryTime(o.getScheduledTime().plusMinutes(o.getLeadTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

            OrderStatusInkatrackerCanonical orderStatus = new OrderStatusInkatrackerCanonical();
            orderStatus.setStatusDate(o.getConfirmedOrder().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            orderStatus.setStatusName(Constant.OrderStatus.CONFIRMED.name());
            orderInfoCanonical.setOrderStatus(orderStatus);

            orderInfoCanonical.setTotalCost(o.getTotalCost().doubleValue());
            orderInfoCanonical.setSubtotal(o.getTotalCost().doubleValue() - o.getDeliveryCost().doubleValue());

            PaymentMethodInkatrackerCanonical paymentMethod =
                    new PaymentMethodInkatrackerCanonical();
            paymentMethod.setType(o.getPaymentType());
            paymentMethod.setChangeAmount((o.getChangeAmount() != null) ? o.getChangeAmount().doubleValue() : 0);
            paymentMethod.setPaidAmount((o.getPaidAmount() != null) ? o.getChangeAmount().doubleValue() : 0);
            paymentMethod.setProvider(o.getCardProvider());
            paymentMethod.setCardCompany("");
            orderInfoCanonical.setPaymentMethod(paymentMethod);

            PreviousStatusCanonical previousStatusCanonical = new PreviousStatusCanonical();
            previousStatusCanonical.setDate(orderStatus.getStatusDate());
            previousStatusCanonical.setOrderStatus(orderStatus.getStatusName());
            orderInfoCanonical.setPreviousStatus(Arrays.asList(previousStatusCanonical));

            orderInfoCanonical.setInkaDeliveryId(o.getExternalId());

            ReceiptInkatrackerCanonical receipt =
                    new ReceiptInkatrackerCanonical();
            receipt.setType(o.getReceiptType());
            receipt.setCompanyAddress(o.getCompanyAddressReceipt());
            receipt.setCompanyId(o.getRuc());
            receipt.setCompanyName(o.getCompanyCode());
            orderInfoCanonical.setReceipt(receipt);

            orderInfoCanonical.setNote(o.getNotes());
            orderInfoCanonical.setCompanyCode(o.getCompanyCode());

            ScheduledCanonical scheduled = new ScheduledCanonical();
            scheduled.setStartDate(o.getScheduledTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            scheduled.setEndDate(orderInfoCanonical.getMaxDeliveryTime());
            orderInfoCanonical.setScheduled(scheduled);

            Map<String, IOrderItemFulfillment> productMap = new HashMap<>();
            orderItemDtoList.forEach(ioProduct -> {
                productMap.put(ioProduct.getProductCode(), ioProduct);
            });

            List<OrderItemInkatrackerCanonical> orderItems =
                    new ArrayList<>();
            productList.forEach(product -> {
                OrderItemInkatrackerCanonical orderItem
                        = new OrderItemInkatrackerCanonical();
                orderItem.setBrand(productMap.get(product.getId()).getBrandProduct());
                orderItem.setFractionated(productMap.get(product.getId()).getFractionated());
                orderItem.setName(productMap.get(product.getId()).getNameProduct());
                orderItem.setQuantity(productMap.get(product.getId()).getQuantity());
                orderItem.setShortDescription(productMap.get(product.getId()).getShortDescriptionProduct());
                orderItem.setSku(productMap.get(product.getId()).getProductCode());
                orderItem.setSap(productMap.get(product.getId()).getProductSapCode());
                orderItem.setEanCode(product.getEanCode());
                orderItem.setTotalPrice(productMap.get(product.getId()).getTotalPrice().doubleValue());
                orderItem.setUnitPrice(productMap.get(product.getId()).getUnitPrice().doubleValue());
                orderItem.setPresentationId(product.getPresentationId());
                orderItem.setPresentationDescription(product.getPresentation());
                orderItem.setQuantityUnits(product.getQuantityUnits());
                orderItems.add(orderItem);
            });
            orderInfoCanonical.setNote(getOrderNotes(o));
            orderInfoCanonical.setOrderItems(orderItems);
        });

        log.debug("[END] map-convertIOrderDtoToOrderFulfillmentCanonical:{}", orderInfoCanonical);
        return orderInfoCanonical;
    }

    private String getOrderNotes(IOrderFulfillment o) {

        Constant.ReceiptType receiptType = Constant.ReceiptType.getByName(o.getReceiptType());

        StringBuilder sb = new StringBuilder();

        if (Constant.ReceiptType.UNDEFINED != receiptType) {
            sb.append(receiptType.getDescription());

            if (Constant.ReceiptType.INVOICE.equals(receiptType)) {
                sb.append(Constant.NOTE_SEPARATOR).append(o.getCompanyNameReceipt())
                        .append(Constant.NOTE_SEPARATOR).append(o.getRuc());
            }
        }

        if (StringUtils.isNotBlank(o.getAddressReceiver())) {
            sb.append(Constant.NOTE_SEPARATOR).append(String.format(Constant.RECEIVER_FORMAT, o.getAddressReceiver()));
        }

        if (sb.length() > Constant.MAX_DELIVERY_NOTES_LENGTH) {
            return sb.toString().substring(0, Constant.MAX_DELIVERY_NOTES_LENGTH);
        }
        return sb.toString();
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
        	//client.setHasInkaClub(o.getInkaClub());

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
            address.setApartment(o.getApartment());
            
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


    public OrderCanonical setsOrderWrapperResponseToOrderCanonical(OrderWrapperResponse orderWrapperResponse,
                                                                   OrderCanonical orderCanonical) {

        // set status
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(orderWrapperResponse.getOrderStatusCode());
        orderStatus.setName(orderWrapperResponse.getOrderStatusName());
        orderStatus.setDetail(orderWrapperResponse.getOrderStatusDetail());
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        orderCanonical.setOrderStatus(orderStatus);

        // set service of delivery or pickup on store
        orderCanonical.getOrderDetail().setServiceCode(orderWrapperResponse.getServiceCode());
        orderCanonical.getOrderDetail().setServiceName(orderWrapperResponse.getServiceName());
        orderCanonical.getOrderDetail().setServiceType(orderWrapperResponse.getServiceType());
        orderCanonical.getOrderDetail().setServiceEnabled(
                Constant.Logical.getByValueString(orderWrapperResponse.getServiceEnabled()).value()
        );
        orderCanonical.getOrderDetail().setServiceSourceChannel(orderWrapperResponse.getServiceSourcechannel());
        orderCanonical.getOrderDetail().setAttempt(orderWrapperResponse.getAttemptBilling());
        orderCanonical.getOrderDetail().setAttemptTracker(orderWrapperResponse.getAttemptTracker());

        // set local and company names;
        orderCanonical.setCompany(orderWrapperResponse.getCompanyName());
        orderCanonical.setLocal(orderWrapperResponse.getLocalName());
        orderCanonical.setLocalCode(orderWrapperResponse.getLocalCode());
        orderCanonical.setLocalDescription(orderWrapperResponse.getLocalDescription());
        orderCanonical.setLocalAddress(orderWrapperResponse.getLocalAddress());
        orderCanonical.setLocalId(orderWrapperResponse.getLocalId());
        orderCanonical.setLocalLongitude(orderWrapperResponse.getLocalLongitude());
        orderCanonical.setLocalLatitude(orderWrapperResponse.getLocalLatitude());

        // attempts
        orderCanonical.setAttemptTracker(orderWrapperResponse.getAttemptTracker());
        orderCanonical.setAttempt(orderWrapperResponse.getAttemptBilling());

        return orderCanonical;

    }

    public Mono<OrderCanonical> convertOrderDtoToOrderCanonical(OrderDto orderDto) {
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

            // For object of inkatrackerlite
            client.setFirstName(r.getFirstName());
            client.setLastName(r.getLastName());
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


            //  For object of inkatrackerlite
            address.setNameAddress(Optional.ofNullable(r.getName()).orElse(StringUtils.EMPTY));
            address.setStreet(Optional.ofNullable(r.getStreet()).orElse(StringUtils.EMPTY));
            address.setNumber(Optional.ofNullable(r.getNumber()).orElse(StringUtils.EMPTY));
            address.setCity(Optional.ofNullable(r.getCity()).orElse(StringUtils.EMPTY));
            address.setApartment(Optional.ofNullable(r.getApartment()).orElse(StringUtils.EMPTY));
            address.setNotes(Optional.ofNullable(r.getNotes()).orElse(StringUtils.EMPTY));
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
            orderDetail.setConfirmedOrder(r.getConfirmedOrder());
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
        receipt.setType(orderDto.getReceipt().getName());
        receipt.setAddress(orderDto.getReceipt().getCompanyAddress());
        receipt.setCompanyName(orderDto.getReceipt().getCompanyName());
        receipt.setRuc(orderDto.getReceipt().getRuc());

        orderCanonical.setReceipt(receipt);

        // set Payment
        PaymentMethodCanonical paymentMethod = new PaymentMethodCanonical();
        paymentMethod.setType(PaymentMethod
                .PaymentType
                .getPaymentTypeByNameType(orderDto.getPayment().getType()).name());
        paymentMethod.setCardProvider(orderDto.getPayment().getCardProvider());
        paymentMethod.setChangeAmount(orderDto.getPayment().getChangeAmount());
        paymentMethod.setPaidAmount(orderDto.getPayment().getPaidAmount());
        orderCanonical.setPaymentMethod(paymentMethod);
        orderCanonical.setSource(orderDto.getSource());
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
