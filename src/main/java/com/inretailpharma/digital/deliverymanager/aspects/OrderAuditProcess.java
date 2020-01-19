package com.inretailpharma.digital.deliverymanager.aspects;


import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
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

    @AfterReturning(value = "execution(* com.inretailpharma.digital.deliverymanager.facade.OrderProcessFacade.createOrder(..))", returning="retVal")
    public void afterCreateOrderFulfillment(JoinPoint joinPoint, Object retVal) {
        log.info("Success aop process afterCreateOrderFulfillment {} - {}",joinPoint.getTarget(), retVal);

            orderExternalService.sendOrder((OrderCanonical)retVal);

    }

    @AfterReturning(value = "execution(* com.inretailpharma.digital.deliverymanager.facade.OrderProcessFacade.getUpdateOrder(..))", returning="retVal")
    public void afterUpdateOrderFulfillment(JoinPoint joinPoint, Object retVal) {
        log.info("Success aop process afterCreateOrderFulfillment {} - {}",joinPoint.getTarget(), retVal);

        // send to audit microservice to update the order
        orderExternalService.updateOrder((OrderCanonical)retVal);

    }

}
