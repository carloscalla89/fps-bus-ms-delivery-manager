package com.inretailpharma.digital.ordermanager.repository;

import com.inretailpharma.digital.ordermanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.ordermanager.entity.ServiceLocalOrderIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceLocalOrderRepository extends JpaRepository<ServiceLocalOrder, ServiceLocalOrderIdentity> {
}
