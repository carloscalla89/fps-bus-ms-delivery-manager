package com.inretailpharma.digital.deliverymanager.config;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalSellerCenterProperties;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ExternalServicesProperties.class,
        ExternalSellerCenterProperties.class
})
public class DeliveryManagerConfig {

}
