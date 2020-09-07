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

@Repository
public interface OrderRepository extends JpaRepository<OrderFulfillment, Long> {

    @Query(value = "select o.ecommerce_purchase_id as ecommerceId, o.tracker_id as trackerId, o.external_purchase_id as externalId, " +
            "o.created_order as createdOrder, o.scheduled_time as scheduledTime, " +
            "cf.document_number as documentNumber, o.total_cost as totalAmount, p.payment_type as paymentMethod, " +
            "ccf.center_code as localCode, ccf.center_name as local, ccf.company_name as company, " +
            "os.code as statusCode, ops.status_detail as statusDetail, os.type as statusType " +
            "from order_fulfillment o " +
            "inner join payment_method p on o.id = p.order_fulfillment_id " +
            "inner join order_process_status ops on ops.order_fulfillment_id = o.id " +
            "inner join order_status os on os.code = ops.order_status_code " +
            "inner join client_fulfillment cf on cf.id = o.client_id " +
            "inner join center_company_fulfillment ccf on ccf.center_code = ops.center_code and ccf.company_code = ops.company_code " +
            "where os.code in :status",
            nativeQuery = true
    )
    List<IOrderFulfillment> getListOrdersByStatus(@Param("status") Set<String> status);


    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.external_purchase_id as externalId, " +
            "ccf.center_code as centerCode, ccf.center_name as centerName, " +
            "ccf.company_code as companyCode, ccf.company_name as companyName, " +
            "st.code as serviceTypeCode, st.name as serviceTypeName, st.type as serviceType, " +
            "o.scheduled_time as confirmedSchedule " +
            "from order_fulfillment o " +
            "inner join order_process_status ops on ops.order_fulfillment_id = o.id " +
            "inner join order_status os on os.code = ops.order_status_code " +
            "inner join service_type st on st.code = ops.service_type_code " +
            "inner join center_company_fulfillment ccf on ccf.center_code = ops.center_code and ccf.company_code = ops.company_code " +
            "where DATE_FORMAT(DATE_ADD(o.scheduled_time, INTERVAL :maxDayPickup DAY), '%Y-%m-%d') < DATE_FORMAT(NOW(), '%Y-%m-%d') " +
            "and st.type = :serviceType and os.type in :statustype and ccf.company_code = :companyCode",
            nativeQuery = true
    )
    List<IOrderFulfillment> getListOrdersToCancel(@Param("serviceType") String serviceType,
                                                  @Param("maxDayPickup") Integer maxDayPickup,
                                                  @Param("companyCode") String companyCode,
                                                  @Param("statustype") Set<String> statustypes);


    @Query(value = "select o.id as orderId, o.ecommerce_purchase_id as ecommerceId, o.tracker_id as trackerId, o.source, " +
            "o.external_purchase_id as externalId, o.bridge_purchase_id as bridgePurchaseId, " +
            "o.total_cost as totalCost, o.delivery_cost as deliveryCost, o.discount_applied as discountApplied, " +
            "o.created_order as createdOrder, o.scheduled_time as scheduledTime, " +
            "o.confirmed_order as confirmedOrder, " +
            "c.first_name as firstName, c.last_name as lastName, c.email, c.document_number as documentNumber, " +
            "c.phone, c.birth_date as birthDate, c.anonimous, c.inkaclub as inkaClub, c.notification_token as notificationToken, " +
            "s.lead_time as leadTime, s.start_hour as startHour, s.end_hour as endHour," +
            "s.order_status_code as statusCode, s.attempt as attempt, s.attempt_tracker as attemptTracker, " +
            "s.center_code as centerCode, s.company_code as companyCode, " +
            "st.code as serviceTypeCode, st.name as serviceTypeName, st.enabled as serviceEnabled," +
            "pm.payment_type as paymentType, pm.card_provider as cardProvider, pm.paid_amount as paidAmount, " +
            "pm.change_amount as changeAmount, " +
            "rt.name as receiptType, rt.document_number as documentNumberReceipt, rt.ruc as ruc, " +
            "rt.company_name as companyNameReceipt, rt.company_address as companyAddressReceipt, rt.receipt_note as noteReceipt," +
            "af.name as addressName, af.street, af.number, af.apartment, af.country, af.city, af.district, af.province, " +
            "af.department, af.notes, af.latitude, af.longitude " +
            "from order_fulfillment o " +
            "inner join client_fulfillment c on c.id = o.client_id " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join service_type st on s.service_type_code = st.code " +
            "inner join payment_method pm on pm.order_fulfillment_id = o.id " +
            "inner join receipt_type rt on rt.order_fulfillment_id = o.id " +
            "inner join address_fulfillment af on af.order_fulfillment_id = o.id " +
            "where o.ecommerce_purchase_id = :ecommerceId",
            nativeQuery = true
    )
    List<IOrderFulfillment> getOrderByecommerceId(@Param("ecommerceId") Long ecommerceId);

    @Query(value ="select oi.product_code as productCode, oi.product_sap_code as productSapCode, oi.name as nameProduct," +
            "oi.short_description as shortDescriptionProduct, oi.brand as brandProduct, oi.quantity, oi.unit_price as unitPrice," +
            "oi.total_price as totalPrice, oi.fractionated, " +
            "oi.ean_code as eanCode, oi.presentation_id as presentationId, oi.presentation_description as presentationDescription, " +
            "oi.quantity_units as quantityUnits, oi.quantity_presentation as quantityPresentation " +
            "from order_fulfillment_item oi " +
            "where oi.order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true
    )
    List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(@Param("orderFulfillmentId") Long orderFulfillmentId);

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment " +
            " set tracker_id = :trackerId " +
            " where id = :orderFulfillmentId",
            nativeQuery = true)
    void updateTrackerId(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                  @Param("trackerId") Long trackerId
    );

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment " +
            " set tracker_id = :trackerId, external_purchase_id = :externalPurchaseId " +
            " where id = :orderFulfillmentId",
            nativeQuery = true)
    void updateExternalAndTrackerId(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                                   @Param("externalPurchaseId") Long externalPurchaseId,
                                                   @Param("trackerId") Long trackerId);

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment " +
            " set external_purchase_id = :externalPurchaseId " +
            " where id = :orderFulfillmentId",
            nativeQuery = true)
    void updateExternalIdToReservedOrder(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                    @Param("externalPurchaseId") Long externalPurchaseId);

    @Query(value = "select o.pay_order_date payOrderDate, o.transaction_order_date transactionOrderDate, o.purchase_number purchaseNumber," +
            "  o.scheduled_order_date as scheduledOrderDate, o.pos_code as posCode" +
    		" from order_fulfillment o where o.ecommerce_purchase_id = :orderNumber",
            nativeQuery = true)
	Optional<IOrderResponseFulfillment> getOrderByOrderNumber(@Param("orderNumber") Long orderNumber);
}
