package com.inretailpharma.digital.ordermanager.config;

import com.inretailpharma.digital.ordermanager.config.parameters.ExternalServicesProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ExternalServicesProperties.class
})
public class OrderManagerConfig {
}
