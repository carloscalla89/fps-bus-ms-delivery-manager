package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    OrderFulfillment getOrderFulfillmentByEcommercePurchaseIdIs(Long ecommerceId);

    @Query(value = "select o.id as orderId, o.tracker_id as trackerId, o.external_purchase_id as externalId, " +
            "o.ecommerce_purchase_id as ecommerceId," +
            "s.order_status_code as statusCode, s.attempt as attempt, s.attempt_tracker as attemptTracker, " +
            "st.code as serviceTypeCode, st.name as serviceTypeName " +
            "from order_fulfillment o " +
            "inner join order_process_status s on o.id = s.order_fulfillment_id " +
            "inner join service_type st on s.service_type_code = st.code " +
            "where o.ecommerce_purchase_id = :ecommerceId",
            nativeQuery = true
    )
    IOrderFulfillment getOrderByecommerceId(@Param("ecommerceId") Long ecommerceId);

    @Modifying
    @Transactional
    @Query(value = "Update order_fulfillment " +
            " set external_purchase_id = :externalPurchaseId " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateExternalPurchaseId(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                  @Param("externalPurchaseId") Long externalPurchaseId
    );

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
}
