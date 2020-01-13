package com.inretailpharma.digital.deliverymanager.aspects;


import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderManagerCanonical;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Aspect
@Configuration
public class OrderAuditProcess {

    private final OrderExternalService orderExternalService;

    public OrderAuditProcess(@Qualifier("audit") OrderExternalService orderExternalService) {
        this.orderExternalService = orderExternalService;
    }

    @AfterReturning(value = "execution(* com.inretailpharma.digital.deliverymanager.facade.*.*(..))", returning="retVal")
    public void afterCreateOrderFulfillment(JoinPoint joinPoint, Object retVal) {
        log.info("Success aop process {} - {}",joinPoint.getTarget(), retVal);

        if (retVal instanceof OrderFulfillmentCanonical) {
            orderExternalService.sendOrder((OrderFulfillmentCanonical)retVal);
        } else {
            // send to audit microservice to update the order
            orderExternalService.updateOrder((OrderManagerCanonical)retVal);
        }


    }
}
