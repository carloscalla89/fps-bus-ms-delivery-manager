package com.inretailpharma.digital.deliverymanager.mapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.AddressInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ClientInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.DrugstoreCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderItemInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderStatusInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PaymentMethodInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PreviousStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ReceiptInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ScheduledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.AddressCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ClientCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.PaymentMethodCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ReceiptCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.Address;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.Client;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillmentItem;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.ReceiptType;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ObjectToMapper {

    public OrderInkatrackerCanonical convertOrderToOrderInkatrackerCanonical(OrderCanonical orderCanonical) {

        OrderInkatrackerCanonical orderInkatrackerCanonical = new OrderInkatrackerCanonical();
        orderInkatrackerCanonical.setOrderExternalId(orderCanonical.getEcommerceId());
        orderInkatrackerCanonical.setLocalCode(orderCanonical.getLocalCode());
        orderInkatrackerCanonical.setCompanyCode(orderCanonical.getCompanyCode());
        Optional.ofNullable(orderCanonical.getOrderDetail())
                .filter(r -> r.getCreatedOrder() != null)
                .ifPresent(r -> orderInkatrackerCanonical.setDateCreated(
                        Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(r.getCreatedOrder())).getTime()
                ));

        orderInkatrackerCanonical.setSource(orderCanonical.getSource());
        Optional.ofNullable(orderCanonical.getDiscountApplied())
                .ifPresent(r -> orderInkatrackerCanonical.setDiscountApplied(r.doubleValue()));

        orderInkatrackerCanonical.setAddress(getFromtOrderCanonical(
                orderCanonical.getAddress(),
                Optional.ofNullable(orderCanonical.getOrderDetail())
                        .filter(r -> r.getLeadTime() != null)
                        .map(OrderDetailCanonical::getLeadTime)
                        .orElse(0))
        );
        orderInkatrackerCanonical.setClient(getFromtOrderCanonical(orderCanonical.getClient()));
        orderInkatrackerCanonical.setDeliveryCost(
                Optional.ofNullable(orderCanonical.getDeliveryCost())
                        .map(BigDecimal::doubleValue)
                        .orElse(null));
        orderInkatrackerCanonical.setDeliveryService(Constant.TrackerImplementation.getByCode(orderCanonical.getOrderDetail().getServiceCode()).getId());
        // para obtener la info del drugstore, se llamará al servicio de fulfillment-center
        orderInkatrackerCanonical.setMaxDeliveryTime(
                Timestamp.valueOf(
                        DateUtils
                                .getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                                .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())
                ).getTime()
        );
        orderInkatrackerCanonical.setOrderItems(createFirebaseOrderItemsFromOrderItemCanonical(orderCanonical.getOrderItems()));
        orderInkatrackerCanonical.setOrderStatus(getFromOrderCanonical(orderCanonical));
        orderInkatrackerCanonical.setTotalCost(orderCanonical.getTotalAmount().doubleValue());
        orderInkatrackerCanonical.setPaymentMethod(getPaymentMethodFromOrderCanonical(orderCanonical));

        PreviousStatusCanonical previousStatus = new PreviousStatusCanonical();
        previousStatus.setDate(orderInkatrackerCanonical.getDateCreated());
        previousStatus.setOrderStatus(orderInkatrackerCanonical.getOrderStatus().getStatusName());
        orderInkatrackerCanonical.setPreviousStatus(Collections.singletonList(previousStatus));

        orderInkatrackerCanonical.setReceipt(getReceiptFromOrderCanonical(orderCanonical));
        ScheduledCanonical scheduledCanonical = new ScheduledCanonical();
        scheduledCanonical.setStartDate(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())).getTime()
        );
        scheduledCanonical.setEndDate(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                        .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())
                ).getTime()
        );
        orderInkatrackerCanonical.setScheduled(scheduledCanonical);

        DrugstoreCanonical drugstoreCanonical = new DrugstoreCanonical();
        drugstoreCanonical.setId(orderCanonical.getLocalId());
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

        orderInkatrackerCanonical.setDrugstore(drugstoreCanonical);
        orderInkatrackerCanonical.setDeliveryType(orderCanonical.getOrderDetail().getServiceShortCode());

        return orderInkatrackerCanonical;
    }

    public OrderInfoInkatrackerLiteCanonical convertOrderToOrderInfoCanonical(OrderCanonical orderCanonical) {
        OrderInfoInkatrackerLiteCanonical orderInfo = new OrderInfoInkatrackerLiteCanonical();

        orderInfo.setOrderExternalId(orderCanonical.getEcommerceId());
        orderInfo.setSource(orderCanonical.getSource());
        Optional.ofNullable(orderCanonical.getOrderDetail())
                .filter(r -> r.getCreatedOrder() != null)
                .ifPresent(r -> orderInfo.setDateCreated(
                        Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(r.getCreatedOrder())).getTime()
                ));

        orderInfo.setInkaDeliveryId(orderCanonical.getExternalId());
        orderInfo.setMaxDeliveryTime(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                        .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())
                ).getTime()
        );

        orderInfo.setDeliveryCost(Optional.ofNullable(orderCanonical.getDeliveryCost()).map(BigDecimal::doubleValue).orElse(0.0));
        orderInfo.setTotalCost(orderCanonical.getTotalAmount().doubleValue());
        orderInfo.setSubtotal(orderCanonical.getSubTotalCost().doubleValue());

        Optional.ofNullable(orderCanonical.getDiscountApplied())
                .ifPresent(orderInfo::setDiscountApplied);

        orderInfo.setClient(getLiteFromtOrderCanonical(orderCanonical.getClient()));
        orderInfo.setNewUserId(orderCanonical.getClient().getNewUserId());
        orderInfo.setDrugstoreId(orderCanonical.getLocalId());
        orderInfo.setAddress(getLiteFromtOrderCanonical(
                orderCanonical.getAddress(),
                Optional.ofNullable(orderCanonical.getOrderDetail())
                        .filter(r -> r.getLeadTime() != null)
                        .map(OrderDetailCanonical::getLeadTime)
                        .orElse(0))
        );

        orderInfo.setPaymentMethod(getPaymentMethodFromOrderCanonical(orderCanonical));

        orderInfo.setOrderItems(getItemsFromOrderItemCanonical(orderCanonical.getOrderItems()));

        orderInfo.setDeliveryServiceId(
                (long) Constant.TrackerImplementation.getByCode(orderCanonical.getOrderDetail().getServiceCode()).getId()
        );

        orderInfo.setStatus(getLiteFromOrderCanonical(orderCanonical, orderInfo));

        orderInfo.setStartDate(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule()))
                        .getTime()
        );
        orderInfo.setEndDate(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedSchedule())
                        .plusMinutes(orderCanonical.getOrderDetail().getLeadTime())).getTime()
        );

        orderInfo.setReceipt(getReceiptFromOrderCanonical(orderCanonical));
        orderInfo.setCallSource(orderCanonical.getSource());
        orderInfo.setDeliveryType(orderCanonical.getOrderDetail().getServiceShortCode());


        orderInfo.setStartHour(orderCanonical.getOrderDetail().getStartHour());
        orderInfo.setEndHour(orderCanonical.getOrderDetail().getEndHour());

        orderInfo.setDaysToPickUp(
                Optional.ofNullable(orderCanonical.getOrderDetail().getDaysToPickup())
                        .map(Object::toString)
                        .orElse("0")
        );

        orderInfo.setPurchaseId(
                Optional.ofNullable(orderCanonical.getPurchaseId())
                        .map(Object::toString)
                        .orElse(null)
        );

        orderInfo.setCompanyCode(orderCanonical.getCompanyCode());
        orderInfo.setLocalCode(orderCanonical.getLocalCode());

        return orderInfo;

    }

    private StatusInkatrackerLiteCanonical getLiteFromOrderCanonical(OrderCanonical orderCanonical,
                                                                     OrderInfoInkatrackerLiteCanonical liteCanonical) {
        StatusInkatrackerLiteCanonical orderStatusInkatrackerCanonical = new StatusInkatrackerLiteCanonical();
        orderStatusInkatrackerCanonical.setDescription(Constant.OrderStatusInkatracker.getByStatusCode(orderCanonical.getOrderStatus().getCode()).getStatus());

        if (orderStatusInkatrackerCanonical.getDescription().equalsIgnoreCase(Constant.OrderStatusInkatracker.CANCEL_ORDER.getStatus())) {

            Optional.ofNullable(orderCanonical.getOrderDetail())
                    .filter(r -> r.getCancelledOrder() != null)
                    .ifPresent(r -> liteCanonical.setCancelDate(
                            Timestamp.valueOf(DateUtils
                                    .getLocalDateTimeFromStringWithFormat(r.getCancelledOrder())).getTime()
                    ));
        }

        return orderStatusInkatrackerCanonical;
    }

    private List<OrderItemInkatrackerLiteCanonical> getItemsFromOrderItemCanonical(List<OrderItemCanonical> itemCanonicals) {

        List<OrderItemInkatrackerLiteCanonical> itemCanonicalList = new ArrayList<>();


        for (OrderItemCanonical itemCanonical : itemCanonicals) {
            OrderItemInkatrackerLiteCanonical canonical = new OrderItemInkatrackerLiteCanonical();
            canonical.setBrand(itemCanonical.getBrand());

            canonical.setFractionated(Optional.ofNullable(itemCanonical.getFractionated()).map(r -> r?"Y":"N").orElse("N"));

            canonical.setName(itemCanonical.getProductName());
            canonical.setQuantity(itemCanonical.getQuantity());
            canonical.setProductId(itemCanonical.getProductCode());
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

    private AddressInkatrackerLiteCanonical getLiteFromtOrderCanonical(AddressCanonical addressCanonical, Integer deliveryTime) {
        AddressInkatrackerLiteCanonical addressInkatrackerCanonical = new AddressInkatrackerLiteCanonical();
        addressInkatrackerCanonical.setName(addressCanonical.getName());
        addressInkatrackerCanonical.setLatitude(
                Optional.ofNullable(addressCanonical.getLatitude())
                        .orElse((BigDecimal.ZERO)).doubleValue());
        addressInkatrackerCanonical.setLongitude(
                Optional.ofNullable(addressCanonical.getLongitude())
                        .orElse((BigDecimal.ZERO)).doubleValue());
        addressInkatrackerCanonical.setCity(addressCanonical.getCity());
        addressInkatrackerCanonical.setDistrict(addressCanonical.getDistrict());
        addressInkatrackerCanonical.setStreet(addressCanonical.getStreet());
        addressInkatrackerCanonical.setNumber(addressCanonical.getNumber());
        addressInkatrackerCanonical.setApartment(addressCanonical.getApartment());
        addressInkatrackerCanonical.setNotes(addressCanonical.getNotes());

        return addressInkatrackerCanonical;
    }

    private ClientInkatrackerLiteCanonical getLiteFromtOrderCanonical(ClientCanonical clientCanonical) {
        ClientInkatrackerLiteCanonical clientInkatrackerCanonical = new ClientInkatrackerLiteCanonical();
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
        clientInkatrackerCanonical.setUserId(clientCanonical.getUserId());
        clientInkatrackerCanonical.setNotificationToken(clientCanonical.getNotificationToken());

        return clientInkatrackerCanonical;
    }

    public OrderStatusCanonical getOrderStatusErrorCancel(String code, String errorDetail) {

        Constant.OrderStatus status = Constant.OrderStatus.getByCode(code);

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(status.getCode());
        orderStatus.setName(status.name());
        orderStatus.setDetail(errorDetail);
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        return orderStatus;
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
        orderStatusInkatrackerCanonical.setStatusName(Constant.OrderStatusInkatracker.getByActionName(orderCanonical.getAction()).getStatus());
        orderStatusInkatrackerCanonical.setStatusDate(
                Timestamp.valueOf(DateUtils.getLocalDateTimeFromStringWithFormat(orderCanonical.getOrderDetail().getConfirmedOrder())).getTime()
        );
        return orderStatusInkatrackerCanonical;
    }

    private List<OrderItemInkatrackerCanonical> createFirebaseOrderItemsFromOrderItemCanonical(List<OrderItemCanonical> itemCanonicals) {

        List<OrderItemInkatrackerCanonical> itemCanonicalList = new ArrayList<>();


        for (OrderItemCanonical itemCanonical : itemCanonicals) {
            OrderItemInkatrackerCanonical canonical = new OrderItemInkatrackerCanonical();
            canonical.setBrand(itemCanonical.getBrand());
            canonical.setFractionated(Optional.ofNullable(itemCanonical.getFractionated()).map(r -> r?"Y":"N").orElse("N"));
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
            canonical.setQuantityUnitMinimium(itemCanonical.getQuantityUnitMinimium());
            canonical.setValueUMV(itemCanonical.getValueUMV());

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
        orderFulfillment.setPurchaseNumber(orderDto.getPurchaseNumber());

        orderFulfillment.setDiscountApplied(orderDto.getDiscountApplied());
        orderFulfillment.setSubTotalCost(orderDto.getSubTotalCost());
        orderFulfillment.setTotalCost(orderDto.getTotalCost());
        orderFulfillment.setDeliveryCost(orderDto.getDeliveryCost());
        orderFulfillment.setSourceCompanyName(orderDto.getSourceCompanyName());

        orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getCreatedOrder()));
        orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getScheduledTime()));
        orderFulfillment.setConfirmedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(orderDto.getSchedules().getConfirmedOrder()));

        Optional.ofNullable(orderDto.getSchedules().getConfirmedInsinkOrder())
                .ifPresent(r -> orderFulfillment.setConfirmedInsinkOrder(DateUtils.getLocalDateTimeFromStringWithFormat(r)));
        Optional.ofNullable(orderDto.getSchedules().getCancelledOrder())
                .ifPresent(r -> orderFulfillment.setCancelledOrder(DateUtils.getLocalDateTimeFromStringWithFormat(r)));

        orderFulfillment.setTransactionOrderDate(orderDto.getSchedules().getTransactionVisaOrder());

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
                    orderFulfillmentItem.setFractionatedPrice(r.getFractionatedPrice());
                    orderFulfillmentItem.setPresentationId(r.getPresentationId());
                    orderFulfillmentItem.setPresentationDescription(r.getPresentationDescription());
                    orderFulfillmentItem.setQuantityUnits(r.getQuantity());
                    orderFulfillmentItem.setQuantityUnitMinimum(r.getQuantityUnitMinimium());
                    orderFulfillmentItem.setQuantityPresentation(r.getQuantityPresentation());
                    orderFulfillmentItem.setFamilyType(r.getFamilyType());
                    orderFulfillmentItem.setValueUMV(r.getValueUMV());
                    return orderFulfillmentItem;
                }).collect(Collectors.toList())
        );

        // object client
        Client client = new Client();
        client.setUserId(orderDto.getClient().getUserId());
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
        client.setNewUserId(orderDto.getClient().getNewUserId());

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

    public OrderCanonical convertIOrderDtoToAndItemOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment,
                                                                             List<IOrderItemFulfillment> itemFulfillments) {
        log.debug("[START] map-convertIOrderDtoToOrderFulfillmentCanonical");

        OrderCanonical orderCanonical = new OrderCanonical();
        Optional.ofNullable(iOrderFulfillment).ifPresent(o -> {

            orderCanonical.setEcommerceId(o.getEcommerceId());
            orderCanonical.setExternalId(o.getExternalId());
            orderCanonical.setPurchaseId(Optional.ofNullable(o.getPurchaseId()).map(Integer::longValue).orElse(null));

            orderCanonical.setTotalAmount(o.getTotalCost());
            orderCanonical.setDeliveryCost(o.getDeliveryCost());
            orderCanonical.setDiscountApplied(o.getDiscountApplied());


            orderCanonical.setCompany(o.getCompanyName());
            orderCanonical.setLocalCode(o.getCenterCode());
            orderCanonical.setLocal(o.getCenterName());

            OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
            orderStatusCanonical.setCode(o.getStatusCode());
            orderCanonical.setOrderStatus(orderStatusCanonical);
            orderCanonical.setSource(o.getSource());

            ClientCanonical client = new ClientCanonical();
            client.setDocumentNumber(o.getDocumentNumber());
            client.setFirstName(o.getFirstName());
            client.setLastName(o.getLastName());
            client.setFullName(Optional.ofNullable(o.getLastName()).orElse(StringUtils.EMPTY)
                    + StringUtils.SPACE + Optional.ofNullable(o.getFirstName()).orElse(StringUtils.EMPTY));
            client.setEmail(o.getEmail());
            client.setPhone(o.getPhone());
            client.setBirthDate(o.getBirthDate());
            client.setAnonimous(
                    Optional.ofNullable(o.getAnonimous())
                            .map(r -> r.equalsIgnoreCase("1")?1:0)
                            .orElse(0));
            client.setUserId(o.getUserId());
            client.setNewUserId(o.getNewUserId());
            client.setNotificationToken(o.getNotificationToken());

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();
            Optional.ofNullable(o.getScheduledTime()).ifPresent(date -> {
                orderDetail.setConfirmedSchedule(DateUtils.getLocalDateTimeWithFormat(date));
            });
            Optional.ofNullable(o.getCreatedOrder()).ifPresent(date -> {
                orderDetail.setCreatedOrder(DateUtils.getLocalDateTimeWithFormat(date));
            });
            Optional.ofNullable(o.getConfirmedOrder()).ifPresent(date -> {
                orderDetail.setConfirmedOrder(DateUtils.getLocalDateTimeWithFormat(date));
            });

            orderDetail.setLeadTime(o.getLeadTime());
            orderDetail.setServiceCode(o.getServiceTypeCode());
            orderDetail.setServiceName(o.getServiceTypeName());

            AddressCanonical address = new AddressCanonical();
            address.setName(o.getAddressName());
            address.setDepartment(o.getDepartment());
            address.setProvince(o.getProvince());
            address.setDistrict(o.getDistrict());
            address.setLatitude(o.getLatitude());
            address.setLongitude(o.getLongitude());
            address.setNotes(o.getNotes());
            address.setApartment(o.getApartment());
            address.setCity(o.getCity());
            address.setNumber(o.getNumber());

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

            List<OrderItemCanonical> itemCanonicals = new ArrayList<>();
            itemFulfillments.stream().forEach(s -> {
                OrderItemCanonical item = new OrderItemCanonical();
                item.setBrand(s.getBrandProduct());
                item.setFractionated(Constant.Logical.Y.name().equals(s.getFractionated()));
                item.setFractionatedPrice(s.getFractionatedPrice());
                item.setProductName(s.getNameProduct());
                item.setQuantity(s.getQuantity());
                item.setProductCode(s.getProductCode());
                item.setProductEan(s.getEanCode());
                item.setTotalPrice(s.getTotalPrice());
                item.setUnitPrice(s.getUnitPrice());
                item.setPresentationId(s.getPresentationId());
                item.setPresentationDescription(s.getPresentationDescription());
                item.setQuantityUnits(s.getQuantityUnits());

                itemCanonicals.add(item);
            });

            orderCanonical.setOrderItems(itemCanonicals);

            orderCanonical.setClient(client);
            orderCanonical.setOrderDetail(orderDetail);
            orderCanonical.setAddress(address);
            orderCanonical.setReceipt(receipt);
            orderCanonical.setPaymentMethod(paymentMethod);
        });

        log.debug("[END] map-convertIOrderDtoToOrderFulfillmentCanonical:{}", orderCanonical);
        return orderCanonical;
    }

    public OrderCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        log.debug("[START] map-convertIOrderDtoToOrderFulfillmentCanonical");
    	
        OrderCanonical orderCanonical = new OrderCanonical();
        Optional.ofNullable(iOrderFulfillment).ifPresent(o -> {
        	
        	orderCanonical.setEcommerceId(o.getEcommerceId());
        	orderCanonical.setExternalId(o.getExternalId());
            orderCanonical.setPurchaseId(Optional.ofNullable(o.getPurchaseId()).map(Integer::longValue).orElse(null));
        	
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
            address.setName(Optional.ofNullable(o.getStreet()).orElse(StringUtils.EMPTY));
            address.setNumber(Optional.ofNullable(o.getNumber()).orElse(StringUtils.EMPTY));
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
                                                                   OrderDto orderDto) {

        OrderCanonical orderCanonical = new OrderCanonical();

        // set ecommerce(shoppingcart) id
        orderCanonical.setEcommerceId(orderDto.getEcommercePurchaseId());

        // Set insink id
        orderCanonical.setExternalId(orderDto.getExternalPurchaseId());

        // Set bridge purchase id(online payment id)
        orderCanonical.setPurchaseId(Optional.ofNullable(orderDto.getPurchaseNumber()).map(Integer::longValue).orElse(null));

        // Set localCode
        orderCanonical.setLocalCode(orderDto.getLocalCode());

        // set total amount
        orderCanonical.setDeliveryCost(orderDto.getDeliveryCost());
        orderCanonical.setDiscountApplied(orderDto.getDiscountApplied());
        orderCanonical.setSubTotalCost(orderDto.getSubTotalCost());
        orderCanonical.setTotalAmount(orderDto.getTotalCost());

        // set status
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(orderWrapperResponse.getOrderStatusCode());
        orderStatus.setName(orderWrapperResponse.getOrderStatusName());
        orderStatus.setDetail(orderWrapperResponse.getOrderStatusDetail());
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        orderCanonical.setOrderStatus(orderStatus);

        // source - example: CALL, WEB, APP
        orderCanonical.setSource(orderDto.getSource());

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
            client.setNotificationToken(r.getNotificationToken());
            // For object of inkatrackerlite
            client.setFirstName(r.getFirstName());
            client.setLastName(r.getLastName());
            client.setUserId(r.getUserId());
            client.setNewUserId(r.getNewUserId());
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
            address.setLatitude(r.getLatitude());
            address.setLongitude(r.getLongitude());
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
                    itemCanonical.setQuantityUnits(r.getQuantityUnits());
                    itemCanonical.setQuantityUnitMinimium(r.getQuantityUnitMinimium());
                    itemCanonical.setUnitPrice(r.getUnitPrice());
                    itemCanonical.setTotalPrice(r.getTotalPrice());
                    itemCanonical.setFractionated(r.getFractionated());
                    itemCanonical.setFractionatedPrice(r.getFractionatedPrice());
                    itemCanonical.setFractionalDiscount(r.getFractionalDiscount());
                    itemCanonical.setQuantityPresentation(r.getQuantityPresentation());
                    itemCanonical.setPresentationId(r.getPresentationId());
                    itemCanonical.setPresentationDescription(r.getPresentationDescription());
                    itemCanonical.setValueUMV(r.getValueUMV());

                    return itemCanonical;
                }).collect(Collectors.toList())
        );

        // set detail order
        OrderDetailCanonical orderDetail = new OrderDetailCanonical();

        Optional.ofNullable(orderDto.getSchedules()).ifPresent(r -> {
            orderDetail.setConfirmedSchedule(r.getScheduledTime());
            orderDetail.setCreatedOrder(r.getCreatedOrder());
            orderDetail.setConfirmedOrder(r.getConfirmedOrder());
            orderDetail.setConfirmedInsinkOrder(r.getScheduledTime());
            orderDetail.setStartHour(r.getStartHour());
            orderDetail.setEndHour(r.getEndHour());
            orderDetail.setLeadTime(r.getLeadTime());
            orderDetail.setTransactionVisaOrder(r.getTransactionVisaOrder());
        });

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

        log.info("[END] convertEntityToOrderCanonical");

        // -------------------------------------------------------------


        // set service of delivery or pickup on store
        orderCanonical.getOrderDetail().setServiceCode(orderWrapperResponse.getServiceCode());
        orderCanonical.getOrderDetail().setServiceShortCode(orderWrapperResponse.getServiceShortCode());
        orderCanonical.getOrderDetail().setServiceName(orderWrapperResponse.getServiceName());
        orderCanonical.getOrderDetail().setServiceType(orderWrapperResponse.getServiceType());
        orderCanonical.getOrderDetail().setServiceEnabled(
                Constant.Logical.getByValueString(orderWrapperResponse.getServiceEnabled()).value()
        );
        orderCanonical.getOrderDetail().setServiceSourceChannel(orderWrapperResponse.getServiceSourcechannel());
        orderCanonical.getOrderDetail().setAttempt(orderWrapperResponse.getAttemptBilling());
        orderCanonical.getOrderDetail().setAttemptTracker(orderWrapperResponse.getAttemptTracker());

        // set local and company names;
        orderCanonical.setCompany(orderWrapperResponse.getCompanyCode());
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
