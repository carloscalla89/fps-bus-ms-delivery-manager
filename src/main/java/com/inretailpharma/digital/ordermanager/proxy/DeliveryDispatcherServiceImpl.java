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
        OrderResultCanonical orderResultCanonical;

        switch (actionOrder.getCode()) {
            case 1:
                // reattempt to send delivery dispatcher at inkatracker or inkatrackerlite
                TrackerResponseDto trackerResponseDto = null;

                try {
                    trackerResponseDto =
                            restTemplate.getForEntity(
                                    externalServicesProperties.getDispatcherTrackerUri().replace("{ecommerceId}", ecommerceId.toString()),
                                    TrackerResponseDto.class).getBody();

                    orderResultCanonical = Optional
                                                .ofNullable(trackerResponseDto)
                                                .filter(r -> r.getOrderExternalId() != null)
                                                .map(r -> {
                                                    OrderResultCanonical resultCanonical = new OrderResultCanonical();
                                                    resultCanonical.setEcommerceId(r.getOrderExternalId());
                                                    return resultCanonical;
                                                }).orElseGet(() -> {
                                                    OrderResultCanonical resultCanonical = new OrderResultCanonical();
                                                    resultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                                                    resultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());

                                                    return resultCanonical;
                                                });

                    orderResultCanonical.setStatusDetail(
                            Optional
                                    .ofNullable(trackerResponseDto)
                                    .map(TrackerResponseDto::getDetail)
                                    .orElse(null)
                    );
                } catch (RestClientException e) {
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherInsinkUri() + "':" + e.getMessage();
                    log.error(errorMessage);
                    orderResultCanonical = new OrderResultCanonical();
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderResultCanonical.setStatusDetail(errorMessage);

                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherTrackerUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    orderResultCanonical = new OrderResultCanonical();
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderResultCanonical.setStatusDetail(errorMessage);
                }

                log.info("object TrackerResponse {}", trackerResponseDto);

                break;

            case 2:
                InsinkResponseCanonical insinkResponseCanonical;
                try {
                    insinkResponseCanonical =
                            restTemplate.getForEntity(
                                    externalServicesProperties.getDispatcherInsinkUri().replace("{ecommerceId}", ecommerceId.toString()),
                                    InsinkResponseCanonical.class).getBody();


                    orderResultCanonical = Optional
                                                .ofNullable(insinkResponseCanonical)
                                                .filter(r -> (r.getErrorCode() == null && r.getInkaventaId() != null))
                                                .map(r -> {
                                                    OrderResultCanonical resultCanonical = new OrderResultCanonical();
                                                    resultCanonical.setExternalId(Long.parseLong(r.getInkaventaId()));
                                                    return resultCanonical;
                                                }).orElseGet(() -> {
                                                    OrderResultCanonical resultCanonical = new OrderResultCanonical();
                                                    resultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                                                    resultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                                                    return resultCanonical;
                                                });

                    orderResultCanonical.setStatusDetail(
                            Optional
                                    .ofNullable(insinkResponseCanonical)
                                    .filter(r -> r.getErrorCode() != null)
                                    .map(InsinkResponseCanonical::getMessageDetail)
                                    .orElse(null)
                    );

                } catch (RestClientException e) {
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherInsinkUri() + "':" + e.getMessage();
                    log.error(errorMessage);

                    orderResultCanonical = new OrderResultCanonical();
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                    orderResultCanonical.setStatusDetail(errorMessage);
                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherInsinkUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    orderResultCanonical = new OrderResultCanonical();
                    orderResultCanonical.setStatusCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                    orderResultCanonical.setStatus(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                    orderResultCanonical.setStatusDetail(errorMessage);
                }

                log.info("object insinkResponse {}", orderResultCanonical);

                break;

            default:
                orderResultCanonical = new OrderResultCanonical();
                orderResultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                orderResultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                break;
        }

        return orderResultCanonical;
    }

}
