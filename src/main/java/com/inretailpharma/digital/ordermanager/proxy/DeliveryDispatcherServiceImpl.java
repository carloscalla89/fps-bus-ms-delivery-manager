package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.dispatcher.InsinkResponseCanonical;
import com.inretailpharma.digital.ordermanager.canonical.dispatcher.TrackerResponseDto;
import com.inretailpharma.digital.ordermanager.canonical.management.OrderResultCanonical;
import com.inretailpharma.digital.ordermanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.ordermanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service("deliveryDispatcher")
public class DeliveryDispatcherServiceImpl implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;

    private RestTemplate restTemplate;

    public DeliveryDispatcherServiceImpl(ExternalServicesProperties externalServicesProperties,
                                         @Qualifier("dispatcherRestTemplate") RestTemplate restTemplate) {
        this.externalServicesProperties = externalServicesProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendOrder(OrderFulfillmentCanonical orderAuditCanonical) {

    }

    @Override
    public OrderResultCanonical updateOrder(Long ecommerceId, Constant.ActionOrder actionOrder) {
        log.info("update order actionOrder.getCode:{}", actionOrder.getCode());
        OrderResultCanonical orderResultCanonical = new OrderResultCanonical();

        switch (actionOrder.getCode()) {
            case 1:
                InsinkResponseCanonical insinkResponseCanonical;
                try {
                    insinkResponseCanonical =
                            restTemplate.getForEntity(
                                    externalServicesProperties.getDispatcherInsinkUri().replace("{ecommerceId}", ecommerceId.toString()),
                                    InsinkResponseCanonical.class).getBody();

                } catch (RestClientException e) {
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherInsinkUri() + "':" + e.getMessage();
                    log.error(errorMessage);

                    insinkResponseCanonical = new InsinkResponseCanonical();
                    insinkResponseCanonical.setErrorCode(Constant.InsinkErrorCode.CODE_ERROR_GENERAL);
                    insinkResponseCanonical.setMessageDetail(errorMessage);
                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherInsinkUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    insinkResponseCanonical = new InsinkResponseCanonical();
                    insinkResponseCanonical.setErrorCode(Constant.InsinkErrorCode.CODE_ERROR_CLIENT_CONNECTION);
                    insinkResponseCanonical.setMessageDetail(errorMessage);
                }

                log.info("object insinkResponse {}",insinkResponseCanonical);


                Optional.ofNullable(insinkResponseCanonical).ifPresent(r -> {
                    orderResultCanonical.setExternalId(
                            Optional
                                    .ofNullable(r.getInkaventaId())
                                    .map(s -> Long.parseLong(r.getInkaventaId()))
                                    .orElse(null));


                    orderResultCanonical.setStatusCode(
                            Optional
                                    .ofNullable(r.getErrorCode())
                                    .map(s -> Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                                    .orElse(null)
                    );
                    orderResultCanonical.setStatus(
                            Optional
                                    .ofNullable(r.getErrorCode())
                                    .map(s -> Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                                    .orElse(null)
                    );

                    orderResultCanonical.setStatusDetail(r.getMessageDetail());

                });

                break;

            case 2:
                // reattempt to send delivery dispatcher at inkatracker or inkatrackerlite
                TrackerResponseDto trackerResponseDto = null;

                try {
                    trackerResponseDto =
                            restTemplate.getForEntity(
                                    externalServicesProperties.getDispatcherTrackerUri().replace("{ecommerceId}", ecommerceId.toString()),
                                    TrackerResponseDto.class).getBody();

                    Optional.ofNullable(trackerResponseDto)
                            .filter(r -> r.getOrderExternalId() != null)
                            .ifPresent(r -> orderResultCanonical.setEcommerceId(r.getOrderExternalId()));

                } catch (RestClientException e) {
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherInsinkUri() + "':" + e.getMessage();
                    log.error(errorMessage);
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderResultCanonical.setStatusDetail(errorMessage);

                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherTrackerUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderResultCanonical.setStatusDetail(errorMessage);
                }

                log.info("object TrackerResponse {}",trackerResponseDto);


                break;

            default:
                orderResultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                orderResultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                break;
        }

        return orderResultCanonical;
    }

}
