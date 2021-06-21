package com.inretailpharma.digital.deliverymanager.mapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.canonical.manager.AddressCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ClientCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.LiquidationDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.*;
import com.inretailpharma.digital.deliverymanager.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.AddressInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ClientInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.DrugstoreCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderItemInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderStatusInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PaymentMethodInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PersonToPickupDto;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PreviousStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ReceiptInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ScheduledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ZoneTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.Address;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.Client;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillmentItem;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.ReceiptType;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ObjectToMapper {


    public LiquidationDto getLiquidationDtoFromOrderCanonical(OrderCanonical orderCanonical,
                                                              LiquidationCanonical liquidationCanonical) {

        LiquidationDto liquidationDto = new LiquidationDto();
        liquidationDto.setEcommerceId(orderCanonical.getEcommerceId().toString());
        liquidationDto.setPurchaseNumber(orderCanonical.getPaymentMethod().getPurchaseNumber());

        liquidationDto.setChannel(orderCanonical.getOrderDetail().getServiceSourceChannel());
        liquidationDto.setCompanyCode(orderCanonical.getCompanyCode());
        liquidationDto.setLocalCode(orderCanonical.getLocalCode());

        liquidationDto.setTransactionVisanet(orderCanonical.getPaymentMethod().getPaymentTransactionId());
        liquidationDto.setTransactionVisanetDate(
                Optional.ofNullable(orderCanonical.getPaymentMethod().getTransactionDateVisanet())
                        .map(DateUtils::getLocalDateTimeFormatUTC).orElse(null)
        );
        liquidationDto.setSource(orderCanonical.getSource());
        liquidationDto.setServiceType(orderCanonical.getOrderDetail().getServiceType());
        liquidationDto.setServiceTypeCode(orderCanonical.getOrderDetail().getServiceShortCode());
        liquidationDto.setLocalType(orderCanonical.getStoreCenter().getLocalType());

        liquidationDto.setStatus(getStatusLiquidation(liquidationCanonical, orderCanonical));
        liquidationDto.setTotalAmount(orderCanonical.getTotalAmount());
        liquidationDto.setChangeAmount(orderCanonical.getPaymentMethod().getChangeAmount());
        liquidationDto.setPaymentMethod(orderCanonical.getPaymentMethod().getType());
        liquidationDto.setCardProvider(orderCanonical.getPaymentMethod().getCardProvider());

        liquidationDto.setFullName(orderCanonical.getClient().getFullName());
        liquidationDto.setDocumentNumber(orderCanonical.getClient().getDocumentNumber());
        liquidationDto.setPhone(orderCanonical.getClient().getPhone());

        return liquidationDto;

    }

    private StatusDto getStatusLiquidation(LiquidationCanonical liquidationCanonical, OrderCanonical orderCanonical) {

        StatusDto statusDto = new StatusDto();
        statusDto.setCode(liquidationCanonical.getCode());
        statusDto.setName(liquidationCanonical.getStatus());
        statusDto.setDetail(orderCanonical.getOrderStatus().getDetail());
        statusDto.setCancellationCode(orderCanonical.getOrderStatus().getCancellationCode());
        statusDto.setCancellationDescription(orderCanonical.getOrderStatus().getCancellationDescription());

        return statusDto;
    }

    public StatusDto getLiquidationStatusDtoFromOrderCanonical(LiquidationCanonical liquidationCanonical,
                                                               OrderCanonical orderCanonical) {

        return getStatusLiquidation(liquidationCanonical, orderCanonical);

    }

    public OrderCanonical getOrderToOrderTracker(IOrderFulfillment iOrderFulfillment,
                                                 List<IOrderItemFulfillment> itemFulfillments) {

        OrderCanonical orderCanonical = convertIOrderDtoToOrderFulfillmentCanonical(iOrderFulfillment);
        orderCanonical.setOrderItems(getItems(itemFulfillments, iOrderFulfillment.getPartial()));

        return orderCanonical;
    }

    public List<OrderItemCanonical> getItems(List<IOrderItemFulfillment> itemFulfillments, Boolean partial) {

        return itemFulfillments.stream().map(item -> {
            OrderItemCanonical orderItem = new OrderItemCanonical();
            orderItem.setProductCode(item.getProductCode());
            orderItem.setProductName(item.getNameProduct());
            orderItem.setSkuSap(item.getProductSapCode());
            orderItem.setShortDescription(item.getShortDescriptionProduct());
            orderItem.setBrand(item.getBrandProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getUnitPrice());
            orderItem.setTotalPrice(item.getTotalPrice());
            orderItem.setFractionated(Constant.Logical.Y.name().equals(item.getFractionated()));
            orderItem.setQuantityUnits(item.getQuantityUnits());
            orderItem.setPresentationId(item.getPresentationId());
            orderItem.setPresentationDescription(item.getPresentationDescription());

            if (Constant.COLLECTION_PRESENTATION_ID.equals(item.getPresentationId())) {

                if (Boolean.TRUE.equals(partial)) {

                    if (Constant.Logical.Y.name().equalsIgnoreCase(item.getFractionated())) {

                        if (item.getQuantityUnits() > 0) {
                            orderItem.setQuantity(item.getQuantity()/item.getQuantityUnits());
                        } else {
                            log.error("[ERROR] convertIOrderItemDtoToOrderItemFulfillmentCanonical orderItem {} invalid QuantityUnits ({}) "
                                    , item.getOrderFulfillmentId(), item.getQuantityUnits());
                            orderItem.setQuantity(item.getQuantity());
                        }

                    } else {
                        orderItem.setQuantity(item.getQuantity());
                    }

                } else {
                    orderItem.setQuantity(item.getQuantityPresentation());
                }
            } else {
                orderItem.setQuantity(item.getQuantity());
            }

            return orderItem;

        }).collect(Collectors.toList());
    }

    public OrderCanonical getOrderFromIOrdersProjects(IOrderFulfillment iOrderFulfillment,
                                                      List<IOrderItemFulfillment> itemFulfillments) {

        OrderCanonical orderCanonical = new OrderCanonical();
        orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
        orderCanonical.setExternalId(iOrderFulfillment.getExternalId());
        orderCanonical.setPurchaseId(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).orElse(null));

        orderCanonical.setTotalAmount(iOrderFulfillment.getTotalCost());
        orderCanonical.setSubTotalCost(iOrderFulfillment.getSubTotalCost());
        orderCanonical.setDeliveryCost(iOrderFulfillment.getDeliveryCost());
        orderCanonical.setCompanyCode(iOrderFulfillment.getCompanyCode());
        orderCanonical.setLocalCode(iOrderFulfillment.getCenterCode());
        orderCanonical.setLocal(iOrderFulfillment.getCenterName());
        orderCanonical.setLocalType(
                Constant.TrackerImplementation
                        .getClassImplement(iOrderFulfillment.getClassImplement())
                        .getLocalType()
        );

        OrderStatusCanonical orderStatusDto = new OrderStatusCanonical();
        orderStatusDto.setCode(iOrderFulfillment.getStatusCode());
        orderStatusDto.setName(iOrderFulfillment.getStatusName());
        orderStatusDto.setDetail(iOrderFulfillment.getStatusDetail());
        orderStatusDto.setCancellationCode(iOrderFulfillment.getCancellationCode());
        orderStatusDto.setCancellationDescription(iOrderFulfillment.getCancellationDescription());
        orderStatusDto.setSuccessful(!Optional.ofNullable(iOrderFulfillment.getStatusDetail()).isPresent());
        orderCanonical.setOrderStatus(orderStatusDto);
        orderCanonical.setOrderItems(getItems(itemFulfillments, iOrderFulfillment.getPartial()));

        return orderCanonical;

    }

    public AuditHistoryDto getAuditHistoryDtoFromObject(OrderCanonical orderCanonical, String updateBy) {

        AuditHistoryDto auditHistoryDto = new AuditHistoryDto();
        auditHistoryDto.setEcommerceId(orderCanonical.getEcommerceId());
        auditHistoryDto.setStatusCode(orderCanonical.getOrderStatus().getCode());
        auditHistoryDto.setStatusName(orderCanonical.getOrderStatus().getName());
        auditHistoryDto.setSource(orderCanonical.getSource());
        auditHistoryDto.setTarget(orderCanonical.getTarget());
        auditHistoryDto.setStatusDetail(orderCanonical.getOrderStatus().getDetail());
        auditHistoryDto.setTimeFromUi(orderCanonical.getOrderStatus().getStatusDate() != null 
        		? orderCanonical.getOrderStatus().getStatusDate() 
        		: DateUtils.getLocalDateTimeNow());
        auditHistoryDto.setOrderNote(orderCanonical.getOrderStatus().getCancellationCode());
        auditHistoryDto.setCustomNote(orderCanonical.getOrderStatus().getCancellationObservation());
        auditHistoryDto.setUpdatedBy(updateBy);
        auditHistoryDto.setLocalCode(orderCanonical.getLocalCode());
        auditHistoryDto.setEndScheduleDate(
                Optional.ofNullable(orderCanonical.getOrderDetail())
                        .filter(res -> res.getConfirmedSchedule() != null && DateUtils.validFormatDateTimeFormat(res.getConfirmedSchedule()))
                        .map(res -> DateUtils
                                        .getLocalDateTimeWithFormat(
                                                DateUtils
                                                    .getLocalDateTimeFromStringWithFormat(
                                                            res.getConfirmedSchedule()
                                                    ).plusMinutes(Optional.ofNullable(res.getLeadTime()).orElse(0))
                                        )
                        ).orElse(null)
        );
        
        auditHistoryDto.setBrand(orderCanonical.getCompanyCode());
        auditHistoryDto.setSourceChannel(orderCanonical.getSource());
        auditHistoryDto.setDeliveryType(
        		 Optional.ofNullable(orderCanonical.getOrderDetail())
        		 		.map(od -> od.getServiceCode()).orElse(null)
        );
        return auditHistoryDto;
    }

    public OrderInkatrackerCanonical convertOrderToOrderInkatrackerCanonical(IOrderFulfillment iOrderFulfillment,
                                                                             List<IOrderItemFulfillment> itemFulfillments,
                                                                             StoreCenterCanonical storeCenterCanonical,
                                                                             Long externalId, String status, String detail,
                                                                             String orderCancelCode, String orderCancelDescription) {

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
        orderInkatrackerCanonical.setExternalChannelId(iOrderFulfillment.getExternalChannelId());

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

        orderInkatrackerCanonical.setDeliveryService(
                Constant.TrackerImplementation.getClassImplement(iOrderFulfillment.getClassImplement()).getId()
        );
        // para obtener la info del drugstore, se llamará al servicio de fulfillment-center

        orderInkatrackerCanonical.setOrderItems(createFirebaseOrderItemsFromOrderItemCanonical(itemFulfillments,storeCenterCanonical.getCompanyCode()));
        // ====set Status to tracker
        orderInkatrackerCanonical.setOrderStatus(
                getFromOrderCanonical(iOrderFulfillment, status, orderCancelCode, orderCancelDescription,orderInkatrackerCanonical, detail));
        orderInkatrackerCanonical.setStatus(
                getFromOrderCanonical(iOrderFulfillment, status, orderCancelCode, orderCancelDescription,orderInkatrackerCanonical, detail));
        // ====

        orderInkatrackerCanonical.setTotalCost(iOrderFulfillment.getTotalCost().doubleValue());
        orderInkatrackerCanonical.setSubtotal(
                Optional.ofNullable(iOrderFulfillment.getTotalCostNoDiscount())
                        .map(BigDecimal::doubleValue)
                        .orElse(iOrderFulfillment.getTotalCost().doubleValue())
        );


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


        orderInkatrackerCanonical.setDeliveryServiceId(
                (long) Constant.TrackerImplementation.getClassImplement(iOrderFulfillment.getClassImplement()).getId()
        );
        orderInkatrackerCanonical.setDrugstoreAddress(storeCenterCanonical.getAddress());
        orderInkatrackerCanonical.setDaysToPickUp(
                Optional.ofNullable(iOrderFulfillment.getDaysPickup())
                        .map(Object::toString)
                        .orElse("0")
        );

        // set personpickup
        orderInkatrackerCanonical.setPersonToPickup(getPersonPickupFromtIOrderFulfillment(iOrderFulfillment));

        orderInkatrackerCanonical.setStartHour(
                Optional.ofNullable(iOrderFulfillment.getStartHour())
                        .map(DateUtils::getLocalTimeWithFormat)
                        .orElse(null)
        );

        orderInkatrackerCanonical.setEndHour(
                Optional.ofNullable(iOrderFulfillment.getEndHour())
                        .map(DateUtils::getLocalTimeWithFormat)
                        .orElse(null)
        );

        orderInkatrackerCanonical.setSourceCompanyName(iOrderFulfillment.getSourceCompanyName());

        orderInkatrackerCanonical.setStockType(
                Optional.ofNullable(iOrderFulfillment.getStockType())
                        .orElse(Constant.StockType.M.name()));

        /**
         * Fecha: 15/04/2021
         * autor: Equipo Growth
         * Campos referentes a 3 precios
         */

        orderInkatrackerCanonical.setSubTotalWithNoSpecificPaymentMethod(
                Optional.ofNullable(iOrderFulfillment.getSubTotalWithNoSpecificPaymentMethod()).map(BigDecimal::doubleValue)
                        .orElse(Constant.VALUE_ZERO_DOUBLE)
        );

        orderInkatrackerCanonical.setTotalWithNoSpecificPaymentMethod(
                Optional.ofNullable(iOrderFulfillment.getTotalWithNoSpecificPaymentMethod()).map(BigDecimal::doubleValue)
                        .orElse(Constant.VALUE_ZERO_DOUBLE)
        );

        orderInkatrackerCanonical.setTotalWithPaymentMethod(
                Optional.ofNullable(iOrderFulfillment.getTotalWithPaymentMethod()).map(BigDecimal::doubleValue)
                        .orElse(Constant.VALUE_ZERO_DOUBLE)
        );

        orderInkatrackerCanonical.setPaymentMethodCardType(
                Optional.ofNullable(iOrderFulfillment.getPaymentMethodCardType())
                        .orElse(Constant.VALUE_ZERO_STRING)
        );

        if(orderInkatrackerCanonical.getTotalWithPaymentMethod()>0
                || orderInkatrackerCanonical.getSubtotal().doubleValue() != orderInkatrackerCanonical.getSubTotalWithNoSpecificPaymentMethod().doubleValue()){
            if(iOrderFulfillment.getDiscountAppliedNoDP()!=null){
                orderInkatrackerCanonical.setDiscountApplied(iOrderFulfillment.getDiscountAppliedNoDP().doubleValue());
                BigDecimal discountAppliedNoDP = BigDecimal.valueOf(iOrderFulfillment.getDiscountAppliedNoDP().doubleValue());
                BigDecimal discountApplied = BigDecimal.valueOf(iOrderFulfillment.getDiscountApplied().doubleValue());
                BigDecimal subTotalCost = BigDecimal.valueOf(iOrderFulfillment.getSubTotalCost().doubleValue());
                orderInkatrackerCanonical.setSubtotal(subTotalCost.subtract(discountAppliedNoDP).subtract(discountApplied).doubleValue());
            }
        }
        /** ********************* **/

        return orderInkatrackerCanonical;
    }


    private Client getClientFromOrderDto(OrderDto orderDto) {
        // object client
        Client client = new Client();

        Optional.ofNullable(orderDto.getClient()).ifPresent(c -> {
            client.setUserId(c.getUserId());
            client.setAnonimous(c.getAnonimous());
            Optional.ofNullable(c.getBirthDate())
                    .ifPresent(r -> client.setBirthDate(DateUtils.getLocalDateFromStringDate(r)));
            client.setEmail(c.getEmail());
            client.setDocumentNumber(c.getDocumentNumber());
            client.setFirstName(c.getFirstName());
            client.setLastName(c.getLastName());
            client.setPhone(c.getPhone());
            client.setInkaclub(c.getHasInkaClub());
            client.setNotificationToken(c.getNotificationToken());
            client.setNewUserId(c.getNewUserId());
        });

        return client;

    }


    public com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto orderFulfillmentToOrderDto(IOrderFulfillment orderFulfillment,
                                                                                                        List<IOrderItemFulfillment> itemsFulfillment,
                                                                                                        StoreCenterCanonical storeCenterCanonical) {
        com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto orderDto = new com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto();

        if (Objects.nonNull(orderFulfillment)) {

            orderDto.setDeliveryServiceName(orderFulfillment.getServiceTypeCode());
            orderDto.setId(String.valueOf(orderFulfillment.getEcommerceId()));
            orderDto.setSource(orderFulfillment.getSource());
            orderDto.setDateCreated(Date.from(orderFulfillment.getCreatedOrder().atZone(ZoneId.systemDefault()).toInstant()));
            orderDto.setOrderDate(Date.from(orderFulfillment.getScheduledTime().atZone(ZoneId.systemDefault()).toInstant()));
            orderDto.setDiscountApplied(
                    Optional.ofNullable(orderFulfillment.getDiscountApplied())
                            .orElse(BigDecimal.ZERO)
            );

            AddressDto address = new AddressDto();
            address.setReceiverName(orderFulfillment.getAddressName());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setLatitude(orderFulfillment.getLatitude().doubleValue());
            address.setLongitude(orderFulfillment.getLongitude().doubleValue());
            address.setNotes(orderFulfillment.getNotes());
            address.setApartment(orderFulfillment.getApartment());
            address.setStreet(orderFulfillment.getStreet());
            address.setCity(orderFulfillment.getCity());
            address.setCountry(orderFulfillment.getCountry());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setNumber(orderFulfillment.getNumber());
            address.setReceiverName(orderFulfillment.getAddressReceiver());
            orderDto.setDeliveryAddress(address);

            UserDto userDto = new UserDto();

            userDto.setDni(orderFulfillment.getDocumentNumber());
            userDto.setName(orderFulfillment.getFirstName());
            userDto.setLastName(orderFulfillment.getLastName());
            userDto.setEmail(orderFulfillment.getEmail());
            userDto.setPhone(orderFulfillment.getPhone());
            userDto.setIsInkaClub(Constant.Logical.N.name());

            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                userDto.setIsAnonymous(Constant.Logical.Y.name());
            } else {
                userDto.setIsAnonymous(Optional.ofNullable(orderFulfillment.getAnonimous())
                        .orElse("0").equals("0") ?"N":"Y");
            }

            orderDto.setUser(userDto);

            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setDeliveryCost(orderFulfillment.getDeliveryCost());
            paymentDto.setDiscountApplied(Optional.ofNullable(orderFulfillment.getDiscountApplied()).orElse(BigDecimal.ZERO));
            paymentDto.setGrossPrice(orderFulfillment.getSubTotalCost());
            paymentDto.setProductsTotalCost(orderFulfillment.getTotalCost());
            paymentDto.setProductsTotalCostNoDiscount(orderFulfillment.getTotalCost());
            paymentDto.setCoupon(orderFulfillment.getCoupon());
            paymentDto.setAmount(orderFulfillment.getPaidAmount());
            orderDto.setPaymentAmountDto(paymentDto);

            PaymentMethodDto paymentMethodDto = new PaymentMethodDto();
            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                paymentMethodDto.setId(Constant.DEFAULT_SC_PAYMENT_METHOD_ID);
                paymentMethodDto.setName(Constant.DEFAULT_SC_PAYMENT_METHOD_VALUE);
            } else {
                paymentMethodDto.setId(
                        PaymentMethod.PaymentType.getPaymentTypeByNameType(orderFulfillment.getPaymentType()).getId()
                );
                paymentMethodDto.setName(
                        PaymentMethod.PaymentType.getPaymentTypeByNameType(orderFulfillment.getPaymentType()).getDescription()
                );
                paymentMethodDto.setBin(orderFulfillment.getBin());
                paymentMethodDto.setCardProvider(orderFulfillment.getCardProvider());
                paymentMethodDto.setCardProviderId(orderFulfillment.getCardProviderId());
                paymentMethodDto.setCardProviderCode(orderFulfillment.getCardProviderCode());
                paymentMethodDto.setBin(orderFulfillment.getBin());
                paymentMethodDto.setPaymentTransactionId(orderFulfillment.getPaymentTransactionId());
            }

            orderDto.setPaymentMethod(paymentMethodDto);

            ReceiptDto receipt = new ReceiptDto();

            ReceiptTypeDto receiptTypeDto = new ReceiptTypeDto();
            receiptTypeDto.setName(orderFulfillment.getReceiptType());

            receipt.setReceiptType(receiptTypeDto);
            receipt.setCompanyAddress(orderFulfillment.getCompanyAddressReceipt());
            receipt.setRuc(orderFulfillment.getRuc());
            receipt.setCompanyName(orderFulfillment.getCompanyNameReceipt());

            orderDto.setReceipt(receipt);
            orderDto.setCompanyCode(orderFulfillment.getCompanyCode());


            List<ItemDto> orderItems = new ArrayList<>();
            for (IOrderItemFulfillment product : itemsFulfillment) {
                ItemDto itemDto = new ItemDto();
                itemDto.setBrand(product.getBrandProduct());
                itemDto.setFractionated(product.getFractionated());
                itemDto.setName(product.getNameProduct());
                itemDto.setQuantity(product.getQuantity());
                itemDto.setShortDescription(product.getShortDescriptionProduct());
                itemDto.setProductId(product.getProductCode());
                itemDto.setProductSapCode(product.getProductSapCode());
                itemDto.setEanCode(product.getEanCode());
                itemDto.setTotalPrice(product.getTotalPrice());
                itemDto.setUnitPrice(product.getUnitPrice());
                itemDto.setPresentationType(product.getPresentationId());
                itemDto.setPresentation(product.getPresentationDescription());
                itemDto.setQuantityUnits(product.getQuantityUnits());
                itemDto.setFamilyType(product.getFamilyType());
                itemDto.setFractionatedPrice(
                        Optional.ofNullable(product.getFractionatedPrice())
                                .map(BigDecimal::doubleValue)
                                .orElse(NumberUtils.DOUBLE_ZERO)
                );
                itemDto.setFractionalDiscount(product.getFractionalDiscount());
                orderItems.add(itemDto);
            }

            orderDto.setItems(orderItems);
            orderDto.setAmount(Optional.ofNullable(orderFulfillment.getPaidAmount()).orElse(BigDecimal.ZERO));
            orderDto.setCreditCardProviderId(Constant.DEFAULT_SC_CARD_PROVIDER_ID);

            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                orderDto.setDeliveryType(Constant.DEFAULT_DS);
            } else {
                orderDto.setDeliveryType(orderFulfillment.getServiceTypeShortCode());
            }

            orderDto.setMarketplaceName(orderFulfillment.getSourceCompanyName());

            DrugstoreDto drugstoreDto = new DrugstoreDto();
            drugstoreDto.setId(storeCenterCanonical.getLegacyId());
            drugstoreDto.setInkaVentaId(storeCenterCanonical.getInkaVentaId());

            orderDto.setDrugstore(drugstoreDto);

            orderDto.setZoneId(orderFulfillment.getZoneId());
            orderDto.setDistrictCode(orderFulfillment.getDistrictCode());
            orderDto.setDeliveryTime(orderFulfillment.getLeadTime());
            orderDto.setCompanyCode(orderFulfillment.getCompanyCode());
        }

        log.info("mapper dto to DD:{}",orderDto);

        return orderDto;
    }

    public ServiceLocalOrder getFromOrderDto(StoreCenterCanonical storeCenterCanonical, OrderDto orderDto,
                                             ServiceType serviceType, String dayToPickup) {
        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();

        serviceLocalOrder.setCenterCode(storeCenterCanonical.getLocalCode());
        serviceLocalOrder.setCompanyCode(storeCenterCanonical.getCompanyCode());

        // Set attempt of attempt to insink and tracker
        serviceLocalOrder.setAttempt(Constant.ONE_ATTEMPT);
        serviceLocalOrder.setAttemptTracker(Constant.ONE_ATTEMPT);

        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));

        Optional.ofNullable(orderDto.getSchedules())
                .ifPresent(s -> {

                    serviceLocalOrder
                            .setStartHour(
                                    Optional.ofNullable(s.getStartHour())
                                            .filter(sh -> DateUtils.getLocalTimeWithValidFormat(sh) != null)
                                            .map(DateUtils::getLocalTimeWithValidFormat)
                                            .orElseGet(() -> storeCenterCanonical
                                                    .getServices()
                                                    .stream()
                                                    .filter(serv -> serv.getCode().equalsIgnoreCase(serviceType.getShortCode()))
                                                    .findFirst()
                                                    .filter(serv -> DateUtils.getLocalTimeWithValidFormat(serv.getStartHour()) != null)
                                                    .map(serv -> DateUtils.getLocalTimeWithValidFormat(serv.getStartHour()))
                                                    .orElse(null)
                                            )
                            );

                    serviceLocalOrder
                            .setEndHour(
                                    Optional.ofNullable(s.getEndHour())
                                            .filter(sh -> DateUtils.getLocalTimeWithValidFormat(sh) != null)
                                            .map(DateUtils::getLocalTimeWithValidFormat)
                                            .orElseGet(() -> storeCenterCanonical
                                                    .getServices()
                                                    .stream()
                                                    .filter(serv -> serv.getCode().equalsIgnoreCase(serviceType.getShortCode()))
                                                    .findFirst()
                                                    .filter(serv -> DateUtils.getLocalTimeWithValidFormat(serv.getEndHour()) != null)
                                                    .map(serv -> DateUtils.getLocalTimeWithValidFormat(serv.getEndHour()))
                                                    .orElse(null)
                                            )
                            );

                    serviceLocalOrder.setDaysToPickup(Optional.ofNullable(dayToPickup).map(Integer::parseInt).orElse(0));

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

    public OrderStatusCanonical getOrderStatus(String name, String errorDetail, String cancellationCode,
                                               String cancellationObservation) {

        Constant.OrderStatus status = Constant.OrderStatus.getByName(name);

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(status.getCode());
        orderStatus.setName(status.name());
        orderStatus.setSuccessful(status.isSuccess());
        orderStatus.setDetail(errorDetail);
        orderStatus.setCancellationCode(cancellationCode);
        orderStatus.setCancellationObservation(cancellationObservation);
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        return orderStatus;
    }

    public OrderStatusCanonical getOrderStatusLiquidation(StatusDto statusDto, String errorDetail, boolean isSuccess) {

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(statusDto.getCode());
        orderStatus.setName(statusDto.getName());
        orderStatus.setDetail(errorDetail);
        orderStatus.setSuccessful(isSuccess);
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

    private OrderStatusInkatrackerCanonical getFromOrderCanonical(IOrderFulfillment iOrderFulfillment, String status,
                                                                  String orderCancelCode, String orderCancelDescription,
                                                                  OrderInkatrackerCanonical orderInkatrackerCanonical,
                                                                  String detail) {
        OrderStatusInkatrackerCanonical orderStatusInkatrackerCanonical = new OrderStatusInkatrackerCanonical();
        orderStatusInkatrackerCanonical.setStatusName(Constant.OrderStatusTracker.getByName(status).getTrackerStatus());
        orderStatusInkatrackerCanonical.setStatusDate(
                Timestamp.valueOf(iOrderFulfillment.getConfirmedOrder()).getTime()
        );
        orderStatusInkatrackerCanonical.setDescription(Constant.OrderStatusTracker.getByName(status).getTrackerStatus());

        if (status != null && (status.equalsIgnoreCase(Constant.OrderStatusTracker.CANCELLED_ORDER.name())
                || status.equalsIgnoreCase(Constant.OrderStatusTracker.CANCELLED_ORDER_ONLINE_PAYMENT.name())
                || status.equalsIgnoreCase(Constant.OrderStatusTracker.CANCELLED_ORDER_NOT_ENOUGH_STOCK.name())
                || status.equalsIgnoreCase(Constant.OrderStatusTracker.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK.name()))) {


            /** inkatracker **/
            orderStatusInkatrackerCanonical.setCode(Optional.ofNullable(orderCancelCode).orElse("EXP"));

            orderStatusInkatrackerCanonical.setCancelDate(
                    Optional.ofNullable(iOrderFulfillment.getCancelledOrder())
                            .map(c -> Timestamp.valueOf(c).getTime())
                            .orElse(Timestamp.valueOf(LocalDateTime.now()).getTime())
            );


            orderStatusInkatrackerCanonical.setCancelReasonCode(orderCancelCode);

            orderStatusInkatrackerCanonical.setCustomNote(
                    Constant.CancellationStockDispatcher.getDetailCancelOrderForStock(status, orderCancelDescription,detail)
            );

            orderStatusInkatrackerCanonical.setCancelMessageNote(
                    Constant.CancellationStockDispatcher.getDetailCancelStock(status, orderCancelDescription)
            );
            /* *************** */

            /** inkatracker-lite **/
            orderInkatrackerCanonical.setCancelDate(
                    Optional.ofNullable(iOrderFulfillment.getCancelledOrder())
                            .map(c -> Timestamp.valueOf(c).getTime())
                            .orElse(Timestamp.valueOf(LocalDateTime.now()).getTime())
            );

            orderInkatrackerCanonical.setCancelReasonCode(Optional.ofNullable(orderCancelCode).orElse("EXP"));
            orderInkatrackerCanonical.setCancelMessageNote(
                    Constant.CancellationStockDispatcher.getDetailCancelOrderForStock(
                            status, orderCancelDescription, detail)
            );

            //
        }

        return orderStatusInkatrackerCanonical;
    }

    private List<OrderItemInkatrackerCanonical> createFirebaseOrderItemsFromOrderItemCanonical(List<IOrderItemFulfillment> itemCanonicals,String companyCode) {

        List<OrderItemInkatrackerCanonical> itemCanonicalList = new ArrayList<>();


        for (IOrderItemFulfillment itemCanonical : itemCanonicals) {
            OrderItemInkatrackerCanonical canonical = new OrderItemInkatrackerCanonical();
            canonical.setBrand(itemCanonical.getBrandProduct());
            canonical.setFractionated(Optional.ofNullable(itemCanonical.getFractionated()).orElse("N"));
            canonical.setName(itemCanonical.getNameProduct());
            canonical.setQuantity(itemCanonical.getQuantity());
            if(companyCode.equalsIgnoreCase(Constant.COMPANY_CODE_MF) && itemCanonical.getProductCodeInkafarma()!=null){
                canonical.setSku(itemCanonical.getProductCodeInkafarma());
                canonical.setProductId(itemCanonical.getProductCodeInkafarma());
            }else{
                canonical.setSku(itemCanonical.getProductCode());
                canonical.setProductId(itemCanonical.getProductCode());
            }
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
            /**
             * Fecha: 15/04/2021
             * autor: Equipo Growth
             * Campos referentes a 3 precios
             */
            canonical.setPriceList(Optional.ofNullable(itemCanonical.getPriceList()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setTotalPriceList(Optional.ofNullable(itemCanonical.getTotalPriceList()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setPriceAllPaymentMethod(Optional.ofNullable(itemCanonical.getPriceAllPaymentMethod()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setTotalPriceAllPaymentMethod(Optional.ofNullable(itemCanonical.getTotalPriceAllPaymentMethod()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setPriceWithpaymentMethod(Optional.ofNullable(itemCanonical.getPriceWithpaymentMethod()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setTotalPriceWithpaymentMethod(Optional.ofNullable(itemCanonical.getTotalPriceWithpaymentMethod()).map(BigDecimal::doubleValue).orElse(Constant.VALUE_ZERO_DOUBLE));
            canonical.setCrossOutPL(itemCanonical.getCrossOutPL());
            canonical.setPaymentMethodCardType(Optional.ofNullable(itemCanonical.getPaymentMethodCardType()).orElse(Constant.VALUE_ZERO_STRING));

            if(canonical.getTotalPriceWithpaymentMethod().doubleValue() > Constant.VALUE_ZERO_DOUBLE){
                canonical.setTotalPrice(canonical.getTotalPriceWithpaymentMethod());
                canonical.setUnitPrice(canonical.getPriceWithpaymentMethod());
            }else if(canonical.getTotalPriceAllPaymentMethod().doubleValue() > Constant.VALUE_ZERO_DOUBLE){
                canonical.setTotalPrice(canonical.getTotalPriceAllPaymentMethod());
                canonical.setUnitPrice(canonical.getPriceAllPaymentMethod());
            }
            /** ** **/
            itemCanonicalList.add(canonical);
        }
        return itemCanonicalList;

    }

    private PersonToPickupDto getPersonPickupFromtIOrderFulfillment(IOrderFulfillment iOrderFulfillment) {

        return  Optional.ofNullable(iOrderFulfillment.getPickupUserId()).map(p -> {
                    PersonToPickupDto personToPickupDto = new PersonToPickupDto();

                    personToPickupDto.setUserId(p);
                    personToPickupDto.setEmail(iOrderFulfillment.getPickupEmail());
                    personToPickupDto.setFullName(iOrderFulfillment.getPickupFullName());
                    personToPickupDto.setIdentityDocumentNumber(iOrderFulfillment.getPickupDocumentNumber());
                    personToPickupDto.setIdentityDocumentType(iOrderFulfillment.getPickupDocumentType());
                    personToPickupDto.setPhone(iOrderFulfillment.getPickupPhone());

                    return personToPickupDto;

                }).orElse(null);

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

    private void setSchedulesAndDates(OrderFulfillment orderFulfillment, OrderDto orderDto) {

        // schedules and dates
        Optional.ofNullable(orderDto.getSchedules().getCreatedOrder())
                .ifPresent(createdOrder -> orderFulfillment.setCreatedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(createdOrder)));

        Optional.ofNullable(orderDto.getSchedules().getScheduledTime())
                .ifPresent(scheduleTime -> orderFulfillment.setScheduledTime(DateUtils.getLocalDateTimeFromStringWithFormat(scheduleTime)));

        Optional.ofNullable(orderDto.getSchedules().getConfirmedOrder())
                .ifPresent(confirmedOrder -> orderFulfillment.setConfirmedOrder(DateUtils.getLocalDateTimeFromStringWithFormat(confirmedOrder)));

        Optional.ofNullable(orderDto.getSchedules().getConfirmedInsinkOrder())
                .ifPresent(confirmedInsinkOrder -> orderFulfillment.setConfirmedInsinkOrder(DateUtils.getLocalDateTimeFromStringWithFormat(confirmedInsinkOrder)));

        Optional.ofNullable(orderDto.getSchedules().getCancelledOrder())
                .ifPresent(cancelledOrder -> orderFulfillment.setCancelledOrder(DateUtils.getLocalDateTimeFromStringWithFormat(cancelledOrder)));

        orderFulfillment.setTransactionOrderDate(orderDto.getSchedules().getTransactionVisaOrder());
    }

    private void setCostsOrder(OrderFulfillment orderFulfillment, OrderDto orderDto){
        orderFulfillment.setDiscountApplied(orderDto.getDiscountApplied());
        orderFulfillment.setSubTotalCost(orderDto.getSubTotalCost());
        orderFulfillment.setTotalCost(orderDto.getTotalCost());
        orderFulfillment.setTotalCostNoDiscount(orderDto.getTotalCostNoDiscount());
        orderFulfillment.setDeliveryCost(orderDto.getDeliveryCost());
    }

    public OrderFulfillment convertOrderdtoToOrderEntity(OrderDto orderDto){
        log.info("[START] map-convertOrderdtoToOrderEntity");

        OrderFulfillment orderFulfillment = new OrderFulfillment();
        orderFulfillment.setSource(orderDto.getSource());
        orderFulfillment.setEcommercePurchaseId(orderDto.getEcommercePurchaseId());
        orderFulfillment.setTrackerId(orderDto.getTrackerId());
        orderFulfillment.setExternalPurchaseId(orderDto.getExternalPurchaseId());
        orderFulfillment.setPurchaseNumber(orderDto.getPurchaseNumber());
        orderFulfillment.setExternalChannelId(orderDto.getExternalChannelId());

        // set Cost from order
        setCostsOrder(orderFulfillment, orderDto);

        orderFulfillment.setSourceCompanyName(orderDto.getSourceCompanyName());

        // schedules and dates data
        setSchedulesAndDates(orderFulfillment, orderDto);

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
                    orderFulfillmentItem.setQuantityUnits(r.getQuantityUnits());
                    orderFulfillmentItem.setQuantityUnitMinimum(r.getQuantityUnitMinimium());
                    orderFulfillmentItem.setQuantityPresentation(r.getQuantityPresentation());
                    orderFulfillmentItem.setFamilyType(r.getFamilyType());
                    orderFulfillmentItem.setValueUMV(r.getValueUMV());

                    /**
                     * Fecha: 15/04/2021
                     * autor: Equipo Growth
                     * Campos referentes a 3 precios
                     */
                    orderFulfillmentItem.setPriceList(r.getPriceList());
                    orderFulfillmentItem.setTotalPriceList(r.getTotalPriceList());
                    orderFulfillmentItem.setPriceAllPaymentMethod(r.getPriceAllPaymentMethod());
                    orderFulfillmentItem.setTotalPriceAllPaymentMethod(r.getTotalPriceAllPaymentMethod());
                    orderFulfillmentItem.setPriceWithpaymentMethod(r.getPriceWithpaymentMethod());
                    orderFulfillmentItem.setTotalPriceWithpaymentMethod(r.getTotalPriceWithpaymentMethod());
                    orderFulfillmentItem.setCrossOutPL(r.isCrossOutPL());
                    orderFulfillmentItem.setPaymentMethodCardType(r.getPaymentMethodCardType());
                    orderFulfillmentItem.setPromotionalDiscount(r.getPromotionalDiscount());

                    /** ************ **/
                    orderFulfillmentItem.setProductCodeInkafarma(r.getProductCodeInkafarma());
                    return orderFulfillmentItem;
                }).collect(Collectors.toList())
        );

        // set client data
        orderFulfillment.setClient(getClientFromOrderDto(orderDto));

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
        paymentMethod.setPaymentTransactionId(orderDto.getPayment().getPaymentTransactionId());
        paymentMethod.setProviderCardCommercialCode(orderDto.getPayment().getProviderCardCommercialCode());
        paymentMethod.setNumPanVisanet(orderDto.getPayment().getNumPanVisaNet());

        Optional.ofNullable(orderDto.getPayment().getTransactionDateVisaNet())
                .filter(res -> GenericValidator.isDate(res, DateUtils.getFormatDateTimeTemplate(), true))
                .ifPresent(res -> paymentMethod.setTransactionDateVisanet(DateUtils.getLocalDateTimeFromStringWithFormat(res)));

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

        orderFulfillment.setStockType(Constant.StockType.getByCode(orderDto.getStockType()).name());

        /**
         * Fecha: 15/04/2021
         * autor: Equipo Growth
         * Campos referentes a 3 precios
         */

        orderFulfillment.setSubTotalWithNoSpecificPaymentMethod(orderDto.getSubTotalWithNoSpecificPaymentMethod());
        orderFulfillment.setTotalWithNoSpecificPaymentMethod(orderDto.getTotalWithNoSpecificPaymentMethod());
        orderFulfillment.setTotalWithPaymentMethod(orderDto.getTotalWithPaymentMethod());
        orderFulfillment.setPaymentMethodCardType(orderDto.getPaymentMethodCardType());
        orderFulfillment.setDiscountAppliedNoDP(orderDto.getDiscountAppliedNoDP());

        /** ************ **/

        log.info("[END] map-convertOrderdtoToOrderEntity");

        return orderFulfillment;
    }

    public OrderCanonical convertIOrderDtoToOrderFulfillmentCanonical(IOrderFulfillment iOrderFulfillment) {
        log.debug("[START] map-convertIOrderDtoToOrderFulfillmentCanonical");
    	
        OrderCanonical orderCanonical = new OrderCanonical();
        Optional.ofNullable(iOrderFulfillment).ifPresent(o -> {

            orderCanonical.setId(iOrderFulfillment.getOrderId());
        	orderCanonical.setEcommerceId(o.getEcommerceId());
        	orderCanonical.setExternalId(o.getExternalId());
            orderCanonical.setPurchaseId(Optional.ofNullable(o.getPurchaseId()).orElse(null));
        	
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

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();
            Optional.ofNullable(o.getScheduledTime()).ifPresent(date -> {
            	orderDetail.setConfirmedSchedule(DateUtils.getLocalDateTimeWithFormat(date));
            });          
            orderDetail.setLeadTime(o.getLeadTime());
            orderDetail.setServiceCode(o.getServiceTypeCode());
            orderDetail.setServiceName(o.getServiceTypeName());
            orderDetail.setServiceType(o.getServiceType());

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
            
            orderCanonical.setPartial(o.getPartial());

        });

        log.debug("[END] map-convertIOrderDtoToOrderFulfillmentCanonical:{}", orderCanonical);
        return orderCanonical;
    }
    



    public OrderCanonical setsOrderWrapperResponseToOrderCanonical(OrderWrapperResponse orderWrapperResponse,
                                                                   OrderDto orderDto) {

        OrderCanonical orderCanonical = new OrderCanonical();

        orderCanonical.setId(orderWrapperResponse.getFulfillmentId());

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
        orderCanonical.setTotalCostNoDiscount(orderDto.getTotalCostNoDiscount());

        // set status
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(orderWrapperResponse.getOrderStatusCode());
        orderStatus.setName(orderWrapperResponse.getOrderStatusName());
        orderStatus.setDetail(orderWrapperResponse.getOrderStatusDetail());
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
        orderStatus.setCancellationCode(orderWrapperResponse.getCancellationCode());
        orderStatus.setCancellationDescription(orderWrapperResponse.getCancellationDescription());
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
                    itemCanonical.setSkuSap(r.getProductSapCode());
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
        paymentMethod.setPaymentTransactionId(orderDto.getPayment().getPaymentTransactionId());
        paymentMethod.setPurchaseNumber(
                Optional.ofNullable(orderDto.getPurchaseNumber()).map(Object::toString).orElse(null)
        );
        paymentMethod.setCardProviderCode(orderDto.getPayment().getCardProviderCode());
        paymentMethod.setNumPanVisanet(orderDto.getPayment().getNumPanVisaNet());
        paymentMethod.setProviderCardCommercialCode(orderDto.getPayment().getProviderCardCommercialCode());

        Optional.ofNullable(orderDto.getPayment().getTransactionDateVisaNet())
                .filter(res -> GenericValidator.isDate(res, DateUtils.getFormatDateTimeTemplate(), true))
                .ifPresent(paymentMethod::setTransactionDateVisanet);

        orderCanonical.setPaymentMethod(paymentMethod);

        log.info("[END] convertEntityToOrderCanonical");

        // -------------------------------------------------------------

        // set service of delivery or pickup on store
        orderCanonical.getOrderDetail().setServiceCode(orderWrapperResponse.getServiceCode());
        orderCanonical.getOrderDetail().setServiceShortCode(orderWrapperResponse.getServiceShortCode());
        orderCanonical.getOrderDetail().setServiceName(orderWrapperResponse.getServiceName());
        orderCanonical.getOrderDetail().setServiceType(orderWrapperResponse.getServiceType());
        orderCanonical.getOrderDetail().setServiceSourceChannel(orderWrapperResponse.getServiceSourcechannel());
        orderCanonical.getOrderDetail().setServiceClassImplement(orderWrapperResponse.getServiceClassImplement());

        orderCanonical.getOrderDetail().setServiceSendNotificationEnabled(
                orderWrapperResponse.isServiceSendNotificationEnabled()
        );

        orderCanonical.getOrderDetail().setServiceEnabled(
                Constant.Logical.getByValueString(orderWrapperResponse.getServiceEnabled()).value()
        );

        orderCanonical.getOrderDetail().setStartHour(
                Optional.ofNullable(orderWrapperResponse.getStartHour())
                                .map(DateUtils::getLocalTimeWithFormat)
                                .orElse(null)
        );

        orderCanonical.getOrderDetail().setEndHour(
                Optional.ofNullable(orderWrapperResponse.getEndHour())
                        .map(DateUtils::getLocalTimeWithFormat)
                        .orElse(null)
        );

        orderCanonical.getOrderDetail().setDaysToPickup(orderWrapperResponse.getDaysToPickup());
        orderCanonical.getOrderDetail().setLeadTime(orderWrapperResponse.getLeadTime());

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

        /*
          Parameters of liquidations
          date: 05-05-21
          by: carlos calla
         */
        orderCanonical.setLiquidation(
                LiquidationCanonical
                        .builder()
                        .enabled(orderWrapperResponse.isLiquidationEnabled())
                        .status(orderWrapperResponse.getLiquidationStatus())
                        .build()
        );
        /* */

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

    public LiquidationCanonical mapLiquidationStatusByEntity(OrderStatus orderStatus) {

        return LiquidationCanonical
                .builder()
                .enabled(orderStatus.isLiquidationEnabled())
                .code(orderStatus.getLiquidationCode())
                .status(orderStatus.getLiquidationStatus())
                .build();

    }
}
