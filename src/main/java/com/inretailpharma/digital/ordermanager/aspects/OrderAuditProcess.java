package com.inretailpharma.digital.ordermanager.aspects;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Aspect
@Configuration
public class OrderAuditProcess {


    @AfterReturning(value = "execution(* com.inretailpharma.digital.ordermanager.facade.*.*(..))", returning="retVal")
    public void afterCreateOrderFulfillment(JoinPoint joinPoint, Object retVal) {
        log.info("Success aop process {} - {}",joinPoint.getTarget(), retVal);
    }
}
