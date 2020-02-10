package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillmentCancelReason;
import com.inretailpharma.digital.deliverymanager.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCancelReasonRepository extends JpaRepository<OrderFulfillmentCancelReason, String>{
}
