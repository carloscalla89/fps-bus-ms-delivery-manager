package com.inretailpharma.digital.deliverymanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<OrderFulfillment, Long> {

    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.external_purchase_id as externalId, " +
            "ops.center_code as centerCode, ops.company_code as companyCode," +
            "st.code as serviceTypeCode, st.name as serviceTypeName, st.type as serviceType, " +
            "o.scheduled_time as scheduledTime , st.class_implement as classImplement, " +
            "st.send_new_flow_enabled as sendNewFlow " +
            "from order_fulfillment o " +
            "inner join order_process_status ops on ops.order_fulfillment_id = o.id " +
            "inner join order_status os on os.code = ops.order_status_code " +
            "inner join service_type st on st.code = ops.service_type_code " +
            "where DATE_FORMAT(DATE_ADD(o.scheduled_time, INTERVAL :maxDayPickup DAY), '%Y-%m-%d') < DATE_FORMAT(NOW(), '%Y-%m-%d') " +
            "and st.type = :serviceType and os.type in :statustype and ops.company_code = :companyCode",
            nativeQuery = true
    )
    List<IOrderFulfillment> getListOrdersToCancel(@Param("serviceType") String serviceType,
                                                  @Param("maxDayPickup") Integer maxDayPickup,
                                                  @Param("companyCode") String companyCode,
                                                  @Param("statustype") Set<String> statustypes);


    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.tracker_id as trackerId, o.source, " +
            "o.external_purchase_id as externalId, o.purchase_number as purchaseId, o.external_channel_id as externalChannelId, " +
            "o.total_cost as totalCost,o.sub_total_cost as subTotalCost, o.delivery_cost as deliveryCost, " +
            "o.discount_applied as discountApplied, o.total_cost_no_discount as totalCostNoDiscount, " +
            "o.created_order as createdOrder, o.scheduled_time as scheduledTime, o.source_company_name as sourceCompanyName, " +
            "o.confirmed_order as confirmedOrder, o.cancelled_order as cancelledOrder, " +
            "o.confirmed_insink_order as confirmedInsinkOrder, o.stockType," +
            "c.first_name as firstName, c.last_name as lastName, c.email, c.document_number as documentNumber, " +
            "c.phone, c.birth_date as birthDate, c.anonimous, c.inkaclub as inkaClub, c.notification_token as notificationToken, " +
            "c.user_id as userId, c.new_user_id as newUserId," +
            "s.lead_time as leadTime, s.start_hour as startHour, s.end_hour as endHour," +
            "s.order_status_code as statusCode, os.type as statusName, s.status_detail as statusDetail," +
            "s.attempt as attempt, s.attempt_tracker as attemptTracker, " +
            "s.center_code as centerCode, s.company_code as companyCode, " +
            "s.zone_id_billing as zoneId, s.district_code_billing as districtCode, s.days_to_pickup as daysPickup, " +
            "s.pickup_user_id as pickupUserId, s.pickup_full_name as pickupFullName, s.pickup_email as pickupEmail," +
            "s.pickup_document_type as pickupDocumentType, s.pickup_document_number as pickupDocumentNumber, " +
            "s.pickup_phone as pickupPhone," +
            "st.code as serviceTypeCode, st.short_code as serviceTypeShortCode,  st.name as serviceTypeName, " +
            "st.enabled as serviceEnabled, st.send_new_code_enabled as newCodeServiceEnabled, st.type as serviceType, " +
            "st.send_new_flow_enabled as sendNewFlow, st.class_implement as classImplement, " +
            "st.send_notification_enabled as sendNotificationByChannel, " +
            "pm.payment_type as paymentType, pm.card_provider as cardProvider, pm.paid_amount as paidAmount, " +
            "pm.change_amount as changeAmount, pm.card_provider_id as cardProviderId, pm.card_provider_code as cardProviderCode," +
            "pm.bin, pm.coupon, pm.payment_transaction_id as paymentTransactionId, " +
            "rt.name as receiptType, rt.document_number as documentNumberReceipt, rt.ruc as ruc, " +
            "rt.company_name as companyNameReceipt, rt.company_address as companyAddressReceipt, rt.receipt_note as noteReceipt," +
            "af.name as addressName, af.street, af.number, af.apartment, af.country, af.city, af.district, af.province, " +
            "af.department, af.notes, af.latitude, af.longitude, o.partial, " +
            "o.subTotalWithNoSpecificPaymentMethod, o.totalWithNoSpecificPaymentMethod, o.totalWithPaymentMethod, " + // referentes a 3 precios
            "o.paymentMethodCardType, o.discountAppliedNoDP " + // referentes a 3 precios
            "os.liquidationEnabled, os.liquidationStatus " +
            "from order_fulfillment o " +
            "inner join client_fulfillment c on c.id = o.client_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join order_status os on os.code = s.order_status_code " +
            "inner join service_type st on s.service_type_code = st.code " +
            "inner join payment_method pm on pm.order_fulfillment_id = o.id " +
            "inner join receipt_type rt on rt.order_fulfillment_id = o.id " +
            "inner join address_fulfillment af on af.order_fulfillment_id = o.id " +
            "where o.ecommerce_purchase_id = :ecommerceId",
            nativeQuery = true
    )
    List<IOrderFulfillment> getOrderByecommerceId(@Param("ecommerceId") Long ecommerceId);

    @Query(value = "SELECT order_status_code as statusCode " +
            "FROM order_fulfillment o " +
            "inner join order_process_status ops on ops.order_fulfillment_id = o.id " +
            "where ecommerce_purchase_id = :ecommerceId order by created_order desc limit 1", nativeQuery = true
    )
    IOrderFulfillment getOnlyOrderStatusByecommerceId(@Param("ecommerceId") Long ecommerceId);

    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.tracker_id as trackerId, o.source, " +
            "o.external_purchase_id as externalId, o.purchase_number as purchaseId, o.external_channel_id as externalChannelId, " +
            "o.total_cost as totalCost,o.sub_total_cost as subTotalCost, o.delivery_cost as deliveryCost, " +
            "o.discount_applied as discountApplied, o.total_cost_no_discount as totalCostNoDiscount, " +
            "o.created_order as createdOrder, o.scheduled_time as scheduledTime, o.source_company_name as sourceCompanyName, " +
            "o.confirmed_order as confirmedOrder, o.cancelled_order as cancelledOrder, " +
            "o.confirmed_insink_order as confirmedInsinkOrder, o.stockType," +
            "c.first_name as firstName, c.last_name as lastName, c.email, c.document_number as documentNumber, " +
            "c.phone, c.birth_date as birthDate, c.anonimous, c.inkaclub as inkaClub, c.notification_token as notificationToken, " +
            "c.user_id as userId, c.new_user_id as newUserId," +
            "s.lead_time as leadTime, s.start_hour as startHour, s.end_hour as endHour," +
            "s.order_status_code as statusCode, os.type as statusName, s.status_detail as statusDetail," +
            "s.attempt as attempt, s.attempt_tracker as attemptTracker, " +
            "s.center_code as centerCode, s.company_code as companyCode, " +
            "s.zone_id_billing as zoneId, s.district_code_billing as districtCode, s.days_to_pickup as daysPickup, " +
            "s.pickup_user_id as pickupUserId, s.pickup_full_name as pickupFullName, s.pickup_email as pickupEmail," +
            "s.pickup_document_type as pickupDocumentType, s.pickup_document_number as pickupDocumentNumber, " +
            "s.pickup_phone as pickupPhone," +
            "st.code as serviceTypeCode, st.short_code as serviceTypeShortCode,  st.name as serviceTypeName, " +
            "st.enabled as serviceEnabled, st.send_new_code_enabled as newCodeServiceEnabled, st.type as serviceType, " +
            "st.send_new_flow_enabled as sendNewFlow, st.class_implement as classImplement, " +
            "st.send_notification_enabled as sendNotificationByChannel, " +
            "pm.payment_type as paymentType, pm.card_provider as cardProvider, pm.paid_amount as paidAmount, " +
            "pm.change_amount as changeAmount, pm.card_provider_id as cardProviderId, pm.card_provider_code as cardProviderCode," +
            "pm.bin, pm.coupon, pm.payment_transaction_id as paymentTransactionId, " +
            "rt.name as receiptType, rt.document_number as documentNumberReceipt, rt.ruc as ruc, " +
            "rt.company_name as companyNameReceipt, rt.company_address as companyAddressReceipt, rt.receipt_note as noteReceipt," +
            "af.name as addressName, af.street, af.number, af.apartment, af.country, af.city, af.district, af.province, " +
            "af.department, af.notes, af.latitude, af.longitude, o.partial, " +
            "o.subTotalWithNoSpecificPaymentMethod, o.totalWithNoSpecificPaymentMethod, o.totalWithPaymentMethod, " + // referentes a 3 precios
            "o.paymentMethodCardType, " + // referentes a 3 precios
            "os.liquidationEnabled, os.liquidationStatus " +
            "from order_fulfillment o " +
            "inner join client_fulfillment c on c.id = o.client_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join order_status os on os.code = s.order_status_code " +
            "inner join service_type st on s.service_type_code = st.code " +
            "inner join payment_method pm on pm.order_fulfillment_id = o.id " +
            "inner join receipt_type rt on rt.order_fulfillment_id = o.id " +
            "inner join address_fulfillment af on af.order_fulfillment_id = o.id " +
            "where o.ecommerce_purchase_id in :ecommercesIds",
            nativeQuery = true
    )
    List<IOrderFulfillment> getOrdersByEcommerceIds(@Param("ecommercesIds") Set<Long> ecommercesIds);

    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.source, " +
            "o.external_purchase_id as externalId, o.tracker_id as trackerId, pm.payment_type as paymentType, " +
            "o.scheduled_time as scheduledTime," +
            "o.total_cost as totalCost,o.sub_total_cost as subTotalCost, o.delivery_cost as deliveryCost, " +
            "os.code as statusCode, os.type as statusName, os.liquidationEnabled, os.liquidationStatus, " +
            "s.status_detail as statusDetail, s.center_code as centerCode, s.company_code as companyCode," +
            "s.cancellation_code as cancellationCode, s.lead_time as leadTime, st.name as serviceTypeName, " +
            "st.type as serviceType, st.code as serviceTypeCode, st.source_channel as serviceChannel, " +
            "st.send_new_flow_enabled as sendNewFlow, st.send_notification_enabled as sendNotificationByChannel, " +
            "st.class_implement as classImplement, st.short_code as serviceTypeShortCode, " +
            "c.first_name as firstName, c.phone, c.document_number as documentNumber, c.email, c.last_name as lastName " +
            "from order_fulfillment o " +
            "inner join client_fulfillment c on c.id = o.client_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join order_status os on os.code = s.order_status_code " +
            "inner join service_type st on st.code = s.service_type_code " +
            "inner join payment_method pm on pm.order_fulfillment_id = o.id " +
            "where o.ecommerce_purchase_id = :ecommerceId",
            nativeQuery = true
    )
    List<IOrderFulfillment> getOrderLightByecommerceId(@Param("ecommerceId") Long ecommerceId);




    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.source, " +
            "o.external_purchase_id as externalId, o.tracker_id as trackerId, pm.payment_type as paymentType, " +
            "o.scheduled_time as scheduledTime," +
            "o.total_cost as totalCost,o.sub_total_cost as subTotalCost, o.delivery_cost as deliveryCost, " +
            "os.code as statusCode, os.type as statusName, os.liquidationEnabled, os.liquidationStatus, " +
            "s.status_detail as statusDetail, s.center_code as centerCode, s.company_code as companyCode," +
            "s.cancellation_code as cancellationCode, " +
            "st.type as serviceType, st.code as serviceTypeCode, st.source_channel as serviceChannel, " +
            "st.send_new_flow_enabled as sendNewFlow, st.send_notification_enabled as sendNotificationByChannel, " +
            "st.class_implement as classImplement, st.short_code as serviceTypeShortCode, " +
            "c.first_name as firstName, c.phone " +
            "from order_fulfillment o " +
            "inner join client_fulfillment c on c.id = o.client_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join order_status os on os.code = s.order_status_code " +
            "inner join service_type st on st.code = s.service_type_code " +
            "inner join payment_method pm on pm.order_fulfillment_id = o.id " +
            "where o.ecommerce_purchase_id in :ecommercesIds",
            nativeQuery = true
    )
    List<IOrderFulfillment> getOrderLightByecommercesIds(@Param("ecommercesIds") Set<Long> ecommercesIds);

    @Query(value ="select oi.order_fulfillment_id as orderFulfillmentId,oi.product_code as productCode, oi.product_sap_code as productSapCode, " +
            "oi.name as nameProduct, oi.short_description as shortDescriptionProduct, oi.brand as brandProduct, oi.quantity, " +
            "oi.unit_price as unitPrice, oi.total_price as totalPrice, oi.fractionated, oi.value_UMV as valueUmv, " +
            "oi.ean_code as eanCode, oi.presentation_id as presentationId, oi.presentation_description as presentationDescription, " +
            "oi.quantity_units as quantityUnits, oi.quantity_presentation as quantityPresentation, oi.quantity_unit_minimium as quantityUnitMinimium," +
            "oi.family_type as familyType, oi.fractionated_price as fractionatedPrice, oi.fractional_discount as fractionalDiscount," +
            "oi.priceList, oi.totalPriceList, oi.priceAllPaymentMethod, oi.totalPriceAllPaymentMethod, oi.priceWithpaymentMethod," +
            "oi.totalPriceWithpaymentMethod, oi.crossOutPL, oi.paymentMethodCardType, oi.promotionalDiscount, oi.product_code_inka as productCodeInkafarma " +
            "from order_fulfillment_item oi " +
            "where oi.order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true
    )
    List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(@Param("orderFulfillmentId") Long orderFulfillmentId);


    @Query(value = "select o.confirmed_order as confirmedOrder, card.card_providerid as creditCardId, " +
            "paymet.payment_method_id as paymentMethodId, " +
            "pay.card_provider,pay.payment_type, " +
            "(case when pay.payment_type = 'CASH_DOLAR' then 'dolar' else 'sol' end) as currency,"+
            "pay.payment_transaction_id as  transactionId, " +
            "os.type as statusName " +
            "from order_fulfillment o inner join " +
            "payment_method pay on o.id=pay.order_fulfillment_id " +
            "inner join payment_method_type paymet on paymet.name=pay.payment_type " +
            "left join card_provider card on pay.card_provider=card.name and paymet.payment_method_id = card.payment_method_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id "+
            "inner join order_status os on os.code = s.order_status_code "+
            "where o.ecommerce_purchase_id = :orderNumber",
            nativeQuery = true)
	Optional<IOrderResponseFulfillment> getOrderByOrderNumber(@Param("orderNumber") Long orderNumber);

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment_item " +
            " set quantity = :quantity ," +
            "  quantity_presentation = :quantity_presentation ," +
            "  unit_Price = :unitPrice ," +
            "  total_Price = :totalPrice ," +
            "  fractionated = :fractionated, " +
            " quantity_units = :quantityUnits, "+
            " presentation_description = :presentation_description, "+
            " presentation_id = :presentation_id "+
            " where order_fulfillment_id = :orderFulfillmentId " +
            " and product_code = :productCode",
            nativeQuery = true)
    void updateItemsPartialOrder(@Param("quantity") Integer quantity,
                                 @Param("quantity_presentation") Integer quantity_presentation,
                                 @Param("unitPrice") BigDecimal unitPrice,
                                 @Param("totalPrice") BigDecimal totalPrice,
                                 @Param("fractionated") String fractionated,
                                 @Param("orderFulfillmentId") Long orderFulfillmentId,
                                 @Param("quantityUnits") Integer quantityUnits,
                                 @Param("productCode") String productCode,
                                 @Param("presentation_description") String presentation_description,
                                 @Param("presentation_id") Integer presentation_id
                                 );

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment " +
            " set total_cost = :totalCost ," +
            "  delivery_cost = :deliveryCost ," +
            "  date_last_updated = :date_last_updated, " +
            "  partial = :partial " +
            " where ecommerce_purchase_id = :externalPurchaseId",
            nativeQuery = true)
    void updatePartialOrder(@Param("totalCost") BigDecimal unitPrice,
                            @Param("deliveryCost") BigDecimal totalPrice,
                            @Param("date_last_updated") LocalDateTime date_last_updated,
                            @Param("externalPurchaseId") Long externalPurchaseId,
                            @Param("partial") boolean partial
    );

    @Modifying
    @Transactional
    @Query(value = " DELETE FROM order_fulfillment_item" +
            " WHERE order_fulfillment_id = :id " +
            " AND  product_code = :productIdsToRemove",
            nativeQuery = true)

    void deleteItemRetired(@Param("productIdsToRemove")String itemId,
                            @Param("id")Long id
                            );

    @Modifying
    @Transactional
    @Query(value = " update payment_method " +
            " set paid_amount = :paidAmount ," +
            " change_amount = :changeAmount," +
            " payment_note = :paymentNote " +
            " WHERE  order_fulfillment_id = :orderId",
            nativeQuery = true)
    void updatePaymentMethod(@Param("paidAmount") BigDecimal paidAmount,
                             @Param("changeAmount") BigDecimal changeAmount,
                             @Param("paymentNote") String paymentNote,
                             @Param("orderId") Long orderId
    );

    @Modifying
    @Query(value = "update payment_method set online_payment_status = :onlinePaymentStatus where order_fulfillment_id = :orderId", nativeQuery = true)
    void updateOnlinePaymentStatusByOrderId(@Param("orderId") Long orderId, @Param("onlinePaymentStatus") String onlinePaymentStatus);
}
