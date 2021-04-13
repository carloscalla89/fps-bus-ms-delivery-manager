package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderFacadeProxy;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryManagerFacadeTest {

    @MockBean
    private OrderTransaction orderTransaction;

    @MockBean
    private ObjectToMapper objectToMapper;

    @MockBean
    private ApplicationParameterService applicationParameterService;

    @MockBean
    private OrderFacadeProxy orderFacadeProxy;

    @Test
    void createOrder() {



    }

    @Test
    void getUpdateOrder() {
    }
}