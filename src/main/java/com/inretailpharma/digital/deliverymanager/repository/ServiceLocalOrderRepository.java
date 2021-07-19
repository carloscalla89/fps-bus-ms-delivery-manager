package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrderIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface ServiceLocalOrderRepository extends JpaRepository<ServiceLocalOrder, ServiceLocalOrderIdentity> {

    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set status_detail = :statusDetail, cancellation_observation = :cancellationObservation, " +
            " cancellation_code = :cancellationCode, order_status_code = :orderStatusCode," +
            " date_last_updated = :dateLastUpdated, date_cancelled = :dateCancelled" +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateStatusCancelledOrder(@Param("statusDetail") String statusDetail,
                                    @Param("cancellationObservation") String cancellationObservation,
                                    @Param("cancellationCode") String cancellationCode,
                                    @Param("orderStatusCode") String orderStatusCode,
                                    @Param("orderFulfillmentId") Long orderFulfillmentId,
                                    @Param("dateLastUpdated") LocalDateTime dateLastUpdated,
                                    @Param("dateCancelled") LocalDateTime dateCancelled

    );

    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set status_detail = :statusDetail, order_status_code = :orderStatusCode, date_last_updated = :dateLastUpdated" +
            " where order_fulfillment_id = " +
            " (select id from order_fulfillment where ecommerce_purchase_id = :ecommerceId order by created_order desc limit 1)",
            nativeQuery = true)
    void updateStatusOrder(@Param("statusDetail") String statusDetail,
                           @Param("orderStatusCode") String orderStatusCode,
                           @Param("ecommerceId") Long ecommerceId,
                           @Param("dateLastUpdated") LocalDateTime dateLastUpdated
    );

}
