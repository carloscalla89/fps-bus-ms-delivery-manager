package com.inretailpharma.digital.deliverymanager.config.parameters;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "external-service.seller-center")
public class ExternalSellerCenterProperties {

    @Value("${external-service.seller-center.host}")
    private String host;

    //  update status service
    @Value("${external-service.seller-center.services.update-status.uri}")
    private String servicesUpdateStatusUri;

    @Value("${external-service.seller-center.services.update-status.connect-timeout}")
    private String servicesUpdateStatusConnectTimeout;

    @Value("${external-service.seller-center.services.update-status.read-timeout}")
    private String servicesUpdateStatusReadTimeout;


    // controversy service
    @Value("${external-service.seller-center.services.add-controversy.uri}")
    private String servicesAddControversyUri;

    @Value("${external-service.seller-center.services.add-controversy.connect-timeout}")
    private String servicesAddControversyConnectTimeout;

    @Value("${external-service.seller-center.services.add-controversy.read-timeout}")
    private String servicesAddControversyReadTimeout;

}
