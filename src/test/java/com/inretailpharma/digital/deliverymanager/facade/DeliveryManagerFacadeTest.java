package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IStoreAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
class DeliveryManagerFacadeTest {

    @Autowired
    private DeliveryManagerFacade deliveryManagerFacade;

    @MockBean
    private OrderTransaction orderTransaction;

    @MockBean
    private ObjectToMapper objectToMapper;

    @MockBean
    private ApplicationParameterService applicationParameterService;

    @MockBean
    private IStoreAdapter iStoreAdapter;

    @MockBean
    private IAuditAdapter iAuditAdapter;

    @MockBean
    private LiquidationFacade liquidationFacade;

    @Test
    void createOrder() {

        OrderDto orderDto = new OrderDto();
        orderDto.setEcommercePurchaseId(123456L);
        orderDto.setLocalCode("FJ7");

        when(iStoreAdapter.getStoreByCompanyCodeAndLocalCode(anyString(), anyString())).thenReturn(Mono.just(new StoreCenterCanonical()));

        when(objectToMapper.convertOrderdtoToOrderEntity(orderDto)).thenReturn(new OrderFulfillment());

        when(orderTransaction.processOrderTransaction(any(OrderFulfillment.class), any(OrderDto.class), any(StoreCenterCanonical.class))).thenReturn(new OrderCanonical());

        when(iAuditAdapter.createAudit(any(OrderCanonical.class), anyString())).thenReturn(Mono.just(new OrderCanonical()));
    }

    @Test
    void getUpdateOrder() {
    }
}