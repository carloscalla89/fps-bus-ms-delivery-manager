package com.inretailpharma.digital.deliverymanager.config;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({
        ExternalServicesProperties.class
})
public class DeliveryManagerConfig {

    @Autowired
    private ExternalServicesProperties externalServicesProperties;



    @Bean(name = "dispatcherRestTemplate")
    public RestTemplate createExternalRestTemplate(RestTemplateBuilder builder) {

        return builder
                .setConnectTimeout(
                        Duration.ofMillis(
                                Long.parseLong(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout())

                        )
                )
                .setReadTimeout(
                        Duration.ofMillis(
                                Long.parseLong(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())
                        )
                ).build();
    }


}
