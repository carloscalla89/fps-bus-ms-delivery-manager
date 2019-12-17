package com.inretailpharma.digital.ordermanager.repository;

import com.inretailpharma.digital.ordermanager.entity.OrderFulfillment;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<OrderFulfillment, Long> {

    @Query(value = "select o.ecommerce_purchase_id as orderId, l.code as localCode, l.name as local, c.name as company, " +
            "o.status as status, o.status_detail as statusDetail, p.payment_type as paymentMethod, " +
            "o.scheduled_time as leadTime, o.document_number as documentNumber, o.total_cost as totalAmount " +
            "from order_fulfillment o " +
            "inner join payment_method p on o.id = p.order_fulfillment_id " +
            "inner join service_local_order s on o.id = s.order_fulfillment_id " +
            "inner join local l on s.local_code = l.code " +
            "inner join company c on l.company_code = c.code " +
            "where o.status in :status",
            nativeQuery = true
    )
    List<IOrderFulfillment> getListOrdersByStatus(@Param("status") Set<String> status);
}
