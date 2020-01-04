package com.inretailpharma.digital.ordermanager.config.parameters;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "external-service")
public class ExternalServicesProperties {


    @Value("${external-service.audit.create-order}")
    private String uriApiService;

    @Value("${external-service.audit.time-out}")
    private Integer timeout;

    @Value("${external-service.dispatcher.insink-tracker.uri}")
    private String dispatcherInsinkTrackerUri;

    @Value("${external-service.dispatcher.insink-tracker.timeout}")
    private String dispatcherInsinkTrackerTimeout;

    @Value("${external-service.dispatcher.tracker.uri}")
    private String dispatcherTrackerUri;

    @Value("${external-service.dispatcher.tracker.timeout}")
    private String dispatcherTrackerTimeout;

}
