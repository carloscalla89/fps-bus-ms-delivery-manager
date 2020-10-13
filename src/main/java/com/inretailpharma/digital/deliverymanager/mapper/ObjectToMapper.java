package com.inretailpharma.digital.deliverymanager.mapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.*;
import com.inretailpharma.digital.deliverymanager.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ObjectToMapper {

    public OrderInkatrackerCanonical convertOrderToOrderInkatrackerCanonical(IOrderFulfillment iOrderFulfillment,
                                                                             List<IOrderItemFulfillment> itemFulfillments,
                                                                             StoreCenterCanonical storeCenterCanonical,
                                                                             Long externalId, String status) {

        OrderInkatrackerCanonical orderInkatrackerCanonical = new OrderInkatrackerCanonical();
        orderInkatrackerCanonical.setOrderExternalId(iOrderFulfillment.getEcommerceId());
        orderInkatrackerCanonical.setInkaDeliveryId(externalId);
        orderInkatrackerCanonical.setSource(iOrderFulfillment.getSource());
        orderInkatrackerCanonical.setCallSource(iOrderFulfillment.getSource());
        orderInkatrackerCanonical.setLocalCode(storeCenterCanonical.getLocalCode());
        orderInkatrackerCanonical.setCompanyCode(storeCenterCanonical.getCompanyCode());

        orderInkatrackerCanonical.setDateCreated(Timestamp.valueOf(iOrderFulfillment.getCreatedOrder()).getTime());
        orderInkatrackerCanonical.setStartDate(Timestamp.valueOf(iOrderFulfillment.getScheduledTime()).getTime());
        orderInkatrackerCanonical.setEndDate(
                Timestamp.valueOf(iOrderFulfillment.getScheduledTime().plusMinutes(iOrderFulfillment.getLeadTime())).getTime());
        orderInkatrackerCanonical.setCancelDate(
                Optional.ofNullable(iOrderFulfillment.getCancelledOrder()).map(c -> Timestamp.valueOf(c).getTime()).orElse(null)
        );

        orderInkatrackerCanonical.setMaxDeliveryTime(
                Timestamp.valueOf(
                        iOrderFulfillment.getScheduledTime()
                                .plusMinutes(iOrderFulfillment.getLeadTime())
                ).getTime()
        );

        Optional.ofNullable(iOrderFulfillment.getDiscountApplied())
                .ifPresent(r -> orderInkatrackerCanonical.setDiscountApplied(r.doubleValue()));

        orderInkatrackerCanonical.setAddress(getFromtOrderCanonical(
                iOrderFulfillment,
                Optional.ofNullable(iOrderFulfillment.getLeadTime()).orElse(0),
                storeCenterCanonical
                )
        );
        orderInkatrackerCanonical.setClient(getFromtOrderCanonical(iOrderFulfillment));
        orderInkatrackerCanonical.setNewUserId(iOrderFulfillment.getNewUserId());
        orderInkatrackerCanonical.setDeliveryCost(
                Optional.ofNullable(iOrderFulfillment.getDeliveryCost())
                        .map(BigDecimal::doubleValue)
                        .orElse(null));

        orderInkatrackerCanonical.setDeliveryService(Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getId());
        // para obtener la info del drugstore, se llamará al servicio de fulfillment-center

        orderInkatrackerCanonical.setOrderItems(createFirebaseOrderItemsFromOrderItemCanonical(itemFulfillments));
        orderInkatrackerCanonical.setOrderStatus(getFromOrderCanonical(iOrderFulfillment, status));
        orderInkatrackerCanonical.setStatus(getFromOrderCanonical(iOrderFulfillment, status));
        orderInkatrackerCanonical.setTotalCost(iOrderFulfillment.getTotalCost().doubleValue());

        orderInkatrackerCanonical.setPaymentMethod(getPaymentMethodFromOrderCanonical(iOrderFulfillment));
        PreviousStatusCanonical previousStatus = new PreviousStatusCanonical();
        previousStatus.setDate(orderInkatrackerCanonical.getDateCreated());
        previousStatus.setOrderStatus(orderInkatrackerCanonical.getOrderStatus().getStatusName());
        orderInkatrackerCanonical.setPreviousStatus(Collections.singletonList(previousStatus));

        orderInkatrackerCanonical.setReceipt(getReceiptFromOrderCanonical(iOrderFulfillment));
        ScheduledCanonical scheduledCanonical = new ScheduledCanonical();
        scheduledCanonical.setStartDate(
                Timestamp.valueOf(iOrderFulfillment.getScheduledTime()).getTime()
        );
        scheduledCanonical.setEndDate(
                Timestamp.valueOf(iOrderFulfillment.getScheduledTime()
                        .plusMinutes(iOrderFulfillment.getLeadTime())
                ).getTime()
        );
        orderInkatrackerCanonical.setScheduled(scheduledCanonical);

        DrugstoreCanonical drugstoreCanonical = new DrugstoreCanonical();
        drugstoreCanonical.setId(storeCenterCanonical.getLegacyId());
        drugstoreCanonical.setName(storeCenterCanonical.getName());
        drugstoreCanonical.setDescription(storeCenterCanonical.getDescription());
        drugstoreCanonical.setAddress(storeCenterCanonical.getAddress());
        drugstoreCanonical.setLatitude(
                Optional.ofNullable(storeCenterCanonical.getLatitude())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0)
        );
        drugstoreCanonical.setLongitude(
                Optional.ofNullable(storeCenterCanonical.getLongitude())
                        .map(BigDecimal::doubleValue)
                        .orElse(0.0)
        );

        orderInkatrackerCanonical.setDrugstore(drugstoreCanonical);
        orderInkatrackerCanonical.setDrugstoreId(storeCenterCanonical.getLegacyId());
        orderInkatrackerCanonical.setDeliveryType(iOrderFulfillment.getServiceTypeShortCode());
        orderInkatrackerCanonical.setDeliveryServiceId((long) Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getId());
        orderInkatrackerCanonical.setDrugstoreAddress(storeCenterCanonical.getAddress());
        orderInkatrackerCanonical.setDaysToPickUp(
                Optional.ofNullable(iOrderFulfillment.getDaysPickup())
                        .map(Object::toString)
                        .orElse("0")
        );

        orderInkatrackerCanonical.setPersonToPickup(getPersonPickupFromtIOrderFulfillment(iOrderFulfillment));


        return orderInkatrackerCanonical;
    }


    public StoreCenterCanonical getStoreCenterFromOrderCanonical(OrderCanonical orderCanonical) {

        StoreCenterCanonical storeCenterCanonical = new StoreCenterCanonical();
        storeCenterCanonical.setLegacyId(orderCanonical.getLocalId());
        storeCenterCanonical.setName(orderCanonical.getLocal());
        storeCenterCanonical.setDescription(orderCanonical.getLocalDescription());
        storeCenterCanonical.setAddress(orderCanonical.getLocalAddress());
        storeCenterCanonical.setCompanyCode(orderCanonical.getCompanyCode());
        storeCenterCanonical.setInkaVentaId(orderCanonical.getInkaVentaId());
        storeCenterCanonical.setLatitude(orderCanonical.getLocalLatitude());
        storeCenterCanonical.setLongitude(orderCanonical.getLocalLongitude());
        storeCenterCanonical.setLocalCode(orderCanonical.getLocalCode());
        storeCenterCanonical.setCompanyCode(orderCanonical.getCompanyCode());

        return storeCenterCanonical;

    }

    public ServiceLocalOrder getFromOrderDto(StoreCenterCanonical storeCenterCanonical, OrderDto orderDto) {
        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();

        serviceLocalOrder.setCenterCode(storeCenterCanonical.getLocalCode());
        serviceLocalOrder.setCompanyCode(storeCenterCanonical.getCompanyCode());



        // Set attempt of attempt to insink and tracker
        serviceLocalOrder.setAttempt(Constant.Constans.ONE_ATTEMPT);
        serviceLocalOrder.setAttemptTracker(Constant.Constans.ONE_ATTEMPT);


        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));

        Optional.ofNullable(orderDto.getSchedules())
                .ifPresent(s -> {
                    serviceLocalOrder.setLeadTime(s.getLeadTime());
                    serviceLocalOrder
                            .setStartHour(
                                    Optional.ofNullable(s.getStartHour())
                                            .filter(sh -> DateUtils.getLocalTimeWithValidFormat(sh) != null)
                                            .map(DateUtils::getLocalTimeWithValidFormat)
                                            .orElse(null));
                    serviceLocalOrder
                            .setEndHour(
                                    Optional.ofNullable(s.getEndHour())
                                            .filter(sh -> DateUtils.getLocalTimeWithValidFormat(sh) != null)
                                            .map(DateUtils::getLocalTimeWithValidFormat)
                                            .orElse(null)
                            );
                    serviceLocalOrder.setDaysToPickup(s.getDaysToPickup());

                });
        serviceLocalOrder.setZoneIdBilling(orderDto.getZoneIdBilling());
        serviceLocalOrder.setDistrictCodeBilling(orderDto.getDistrictCodeBilling());

        Optional.ofNullable(orderDto.getPersonToPickup()).ifPresent(p -> {
            serviceLocalOrder.setPickupDocumentNumber(p.getIdentityDocumentNumber());
            serviceLocalOrder.setPickupDocumentType(p.getIdentityDocumentType());
            serviceLocalOrder.setPickupEmail(p.getEmail());
            serviceLocalOrder.setPickupFullName(p.getFullName());
            serviceLocalOrder.setPickupEmail(p.getEmail());
            serviceLocalOrder.setPickupUserId(p.getUserId());
        });

        return serviceLocalOrder;
    }

    public OrderStatusCanonical getOrderStatusInkatracker(String name, String errorDetail) {

        Constant.OrderStatus status = Constant.OrderStatusTracker.getByName(name).getOrderStatus();

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(status.getCode());
        orderStatus.setName(status.name());
        orderStatus.setDetail(errorDetail);
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        return orderStatus;
    }

    private ReceiptInkatrackerCanonical getReceiptFromOrderCanonical(IOrderFulfillment orderCanonical) {
        ReceiptInkatrackerCanonical receiptInkatrackerCanonical = new ReceiptInkatrackerCanonical();
        receiptInkatrackerCanonical.setType(orderCanonical.getReceiptType());
        receiptInkatrackerCanonical.setCompanyId(orderCanonical.getRuc());
        receiptInkatrackerCanonical.setCompanyName(orderCanonical.getCompanyNameReceipt());
        receiptInkatrackerCanonical.setCompanyAddress(orderCanonical.getCompanyAddressReceipt());
        receiptInkatrackerCanonical.setNote(
                orderCanonical.getReceiptType()
                        + Optional.ofNullable(orderCanonical.getReceiptType())
                                  .filter(r -> r.equalsIgnoreCase(Constant.Receipt.INVOICE))
                                  .map(r -> " - " + orderCanonical.getRuc())
                                  .orElse(""));
        return receiptInkatrackerCanonical;
    }

    private PaymentMethodInkatrackerCanonical getPaymentMethodFromOrderCanonical(IOrderFulfillment orderCanonical) {
        PaymentMethodInkatrackerCanonical canonical = new PaymentMethodInkatrackerCanonical();
        canonical.setType(orderCanonical.getPaymentType());
        canonical.setNote(orderCanonical.getPaymentType());
        canonical.setPaidAmount(
                Optional.ofNullable(orderCanonical.getPaidAmount())
                        .map(BigDecimal::doubleValue)
                        .orElse(null)
        );
        canonical.setChangeAmount(
                Optional.ofNullable(orderCanonical.getChangeAmount())
                        .map(BigDecimal::doubleValue)
                        .orElse(null)
        );
        canonical.setProvider(orderCanonical.getCardProvider());

        return canonical;
    }

    private OrderStatusInkatrackerCanonical getFromOrderCanonical(IOrderFulfillment iOrderFulfillment, String status) {
        OrderStatusInkatrackerCanonical orderStatusInkatrackerCanonical = new OrderStatusInkatrackerCanonical();
        orderStatusInkatrackerCanonical.setStatusName(Constant.OrderStatusTracker.getByName(status).getStatus());
        orderStatusInkatrackerCanonical.setStatusDate(
                Timestamp.valueOf(iOrderFulfillment.getScheduledTime()).getTime()
        );
        orderStatusInkatrackerCanonical.setDescription(Constant.OrderStatusTracker.getByName(status).getStatus());

        return orderStatusInkatrackerCanonical;
    }

    private List<OrderItemInkatrackerCanonical> createFirebaseOrderItemsFromOrderItemCanonical(List<IOrderItemFulfillment> itemCanonicals) {

        List<OrderItemInkatrackerCanonical> itemCanonicalList = new ArrayList<>();


        for (IOrderItemFulfillment itemCanonical : itemCanonicals) {
            OrderItemInkatrackerCanonical canonical = new OrderItemInkatrackerCanonical();
            canonical.setBrand(itemCanonical.getBrandProduct());
            canonical.setFractionated(
                    Optional.ofNullable(itemCanonical.getFractionated())
                            .filter(f -> f.equalsIgnoreCase("1"))
                            .map(f -> "Y")
                            .orElse("N")
            );
            canonical.setName(itemCanonical.getNameProduct());
            canonical.setQuantity(itemCanonical.getQuantity());
            canonical.setSku(itemCanonical.getProductCode());
            canonical.setProductId(itemCanonical.getProductCode());
            canonical.setEanCode(itemCanonical.getEanCode());
            canonical.setTotalPrice(itemCanonical.getTotalPrice().doubleValue());
            canonical.setUnitPrice(itemCanonical.getUnitPrice().doubleValue());
            canonical.setWithStock(Constant.Logical.Y.name());
            canonical.setPresentationId(itemCanonical.getPresentationId());
            canonical.setPresentationDescription(itemCanonical.getPresentationDescription());
            canonical.setQuantityUnits(itemCanonical.getQuantityUnits());
            canonical.setShortDescription(itemCanonical.getShortDescriptionProduct());
            canonical.setQuantityPresentation(itemCanonical.getQuantityPresentation());
            canonical.setQuantityUnitMinimium(itemCanonical.getQuantityUnitMinimium());
            canonical.setValueUMV(itemCanonical.getValueUmv());
            canonical.setSap(itemCanonical.getProductSapCode());

            itemCanonicalList.add(canonical);
        }
        return itemCanonicalList;

    }

    private PersonToPickupDto getPersonPickupFromtIOrderFulfillment(IOrderFulfillment iOrderFulfillment) {
        PersonToPickupDto personToPickupDto = new PersonToPickupDto();
        personToPickupDto.setUserId(iOrderFulfillment.getPickupUserId());
        personToPickupDto.setEmail(iOrderFulfillment.getPickupEmail());
        personToPickupDto.setFullName(iOrderFulfillment.getPickupFullName());
        personToPickupDto.setIdentityDocumentNumber(iOrderFulfillment.getPickupDocumentType());
        personToPickupDto.setIdentityDocumentType(iOrderFulfillment.getPickupDocumentType());
        personToPickupDto.setPhone(iOrderFulfillment.getPickupPhone());

        return personToPickupDto;
    }

    private AddressInkatrackerCanonical getFromtOrderCanonical(IOrderFulfillment addressCanonical, Integer deliveryTime,
                                                               StoreCenterCanonical centerCanonical) {
        AddressInkatrackerCanonical addressInkatrackerCanonical = new AddressInkatrackerCanonical();
        addressInkatrackerCanonical.setName(addressCanonical.getAddressName());

        if (Optional
                .ofNullable(addressCanonical.getServiceTypeShortCode())
                .orElse("RAD").equalsIgnoreCase("RET")) {
            addressInkatrackerCanonical.setLatitude(
                    Optional.ofNullable(centerCanonical.getLatitude())
                            .orElse((BigDecimal.ZERO)).doubleValue());
            addressInkatrackerCanonical.setLongitude(
                    Optional.ofNullable(centerCanonical.getLongitude())
                            .orElse((BigDecimal.ZERO)).doubleValue());
        } else {
            addressInkatrackerCanonical.setLatitude(
                    Optional.ofNullable(addressCanonical.getLatitude())
                            .orElse((BigDecimal.ZERO)).doubleValue());
            addressInkatrackerCanonical.setLongitude(
                    Optional.ofNullable(addressCanonical.getLongitude())
                            .orElse((BigDecimal.ZERO)).doubleValue());
        }

        addressInkatrackerCanonical.setCity(addressCanonical.getCity());
        addressInkatrackerCanonical.setDistrict(addressCanonical.getDistrict());
        addressInkatrackerCanonical.setStreet(addressCanonical.getStreet());
        addressInkatrackerCanonical.setNumber(addressCanonical.getNumber());
        addressInkatrackerCanonical.setApartment(addressCanonical.getApartment());
        addressInkatrackerCanonical.setNotes(addressCanonical.getNotes());
        addressInkatrackerCanonical.setZoneEta(deliveryTime);

        ZoneTrackerCanonical zoneTrackerCanonical = new ZoneTrackerCanonical();
        zoneTrackerCanonical.setId(addressCanonical.getZoneId());

        addressInkatrackerCanonical.setZone(zoneTrackerCanonical);

        return addressInkatrackerCanonical;
    }

    private ClientInkatrackerCanonical getFromtOrderCanonical(IOrderFulfillment clientCanonical) {
        ClientInkatrackerCanonical clientInkatrackerCanonical = new ClientInkatrackerCanonical();
        Optional.ofNullable(clientCanonical.getBirthDate()).ifPresent(r ->
                clientInkatrackerCanonical.setBirthDate(DateUtils.getLocalDateFromStringDate(r).toEpochDay()));

        clientInkatrackerCanonical.setDni(clientCanonical.getDocumentNumber());
        clientInkatrackerCanonical.setEmail(clientCanonical.getEmail());
        clientInkatrackerCanonical.setFirstName(clientCanonical.getFirstName());
        clientInkatrackerCanonical.setLastName(Optional.ofNullable(clientCanonical.getLastName()).orElse(StringUtils.EMPTY));
        clientInkatrackerCanonical.setPhone(clientCanonical.getPhone());
        clientInkatrackerCanonical.setIsAnonymous(
                Optional.ofNullable(clientCanonical.getAnonimous())
                        .orElse("0").equalsIgnoreCase("0")?"N":"Y"
        );
        clientInkatrackerCanonical.setHasInkaClub(
                Optional.ofNullable(clientCanonical.getInkaClub())
                        .orElse("0").equalsIgnoreCase("0")?"N":"Y"
        );
        clientInkatrackerCanonical.setUserId(clientCanonical.getUserId());
        clientInkatrackerCanonical.setJoinIdentifierId(clientCanonical.getNewUserId());
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

        // object orderItems
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
        paymentMethod.setBin(orderDto.getPayment().getBin());
        paymentMethod.setCardProviderId(orderDto.getPayment().getCardProviderId());
        paymentMethod.setCardProvider(orderDto.getPayment().getCardProvider());
        paymentMethod.setCardProviderCode(orderDto.getPayment().getCardProviderCode());
        paymentMethod.setPaidAmount(orderDto.getPayment().getPaidAmount());
        paymentMethod.setChangeAmount(orderDto.getPayment().getChangeAmount());
        paymentMethod.setCoupon(orderDto.getPayment().getCoupon());

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
                    itemCanonical.setSap(r.getProductSapCode());
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
        orderCanonical.setCompanyCode(orderWrapperResponse.getCompanyCode());
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
