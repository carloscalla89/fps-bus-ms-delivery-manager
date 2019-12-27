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

    @Value("${external-service.dispatcher.insink.uri}")
    private String dispatcherInsinkUri;

    @Value("${external-service.dispatcher.insink.timeout}")
    private String dispatcherInsinkTimeout;

}
