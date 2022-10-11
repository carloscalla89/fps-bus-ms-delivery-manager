package com.inretailpharma.digital.deliverymanager.config.parameters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "routing")
public class RoutingProperties {
	
    @Value("${routing.username}")
    private String username;

    @Value("${routing.password}")
    private String password;

    @Value("${routing.client-id}")
    private String clientId;

}
