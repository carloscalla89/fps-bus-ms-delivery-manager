package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrderIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ServiceLocalOrderRepository extends JpaRepository<ServiceLocalOrder, ServiceLocalOrderIdentity> {

    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set attempt = :attempt, " +
            " order_status_code = :orderStatusCode, status_detail = :statusDetail " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateReattemtpInsink(@Param("orderFulfillmentId") Long orderFulfillmentId,
                               @Param("attempt") Integer attempt,
                               @Param("orderStatusCode") String orderStatusCode,
                               @Param("statusDetail") String statusDetail
    );

    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set attempt_tracker = :attemptTracker, " +
            " order_status_code = :orderStatusCode, status_detail = :statusDetail " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateReattemtpTracker(@Param("orderFulfillmentId") Long orderFulfillmentId,
                               @Param("attemptTracker") Integer attemptTracker,
                               @Param("orderStatusCode") String orderStatusCode,
                               @Param("statusDetail") String statusDetail
    );


    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set attempt_tracker = :attemptTracker, attempt = :attempt, " +
            " order_status_code = :orderStatusCode, status_detail = :statusDetail " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateRetryingOrderStatusProcess(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                          @Param("attemptTracker") Integer attemptTracker,
                                          @Param("attempt") Integer attempt,
                                          @Param("orderStatusCode") String orderStatusCode,
                                          @Param("statusDetail") String statusDetail
    );

    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set attempt = :attempt, " +
            " order_status_code = :orderStatusCode, status_detail = :statusDetail " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateStatusToReservedOrder(@Param("orderFulfillmentId") Long orderFulfillmentId,
                                     @Param("attempt") Integer attempt,
                                     @Param("orderStatusCode") String orderStatusCode,
                                     @Param("statusDetail") String statusDetail
    );


    @Modifying
    @Transactional
    @Query(value = "Update order_process_status " +
            " set order_status_code = :orderStatusCode, status_detail = :statusDetail " +
            " where order_fulfillment_id = :orderFulfillmentId",
            nativeQuery = true)
    void updateStatusOrder(@Param("orderFulfillmentId") Long orderFulfillmentId,
                           @Param("orderStatusCode") String orderStatusCode,
                           @Param("statusDetail") String statusDetail
    );

}
