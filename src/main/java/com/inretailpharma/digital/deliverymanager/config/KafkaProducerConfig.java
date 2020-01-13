package com.inretailpharma.digital.deliverymanager.config;



import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProducerConfig {
/*
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaServers;

    @Value("${spring.kafka.confluent.api-key}")
    private String confluentApiKey;

    @Value("${spring.kafka.confluent.api-secret}")
    private String confluentApiSecret;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put("ssl.endpoint.identification.algorithm","https");
        props.put("sasl.mechanism","PLAIN");
        props.put("retry.backoff.ms","500");
        props.put("sasl.jaas.config",String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",confluentApiKey,confluentApiSecret));
        props.put("security.protocol","SASL_SSL");
        props.put("basic.auth.credentials.source","USER_INFO");
        props.put("retries", 0);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaJsonSerializer");

        return props;
    }

    @Bean
    public ProducerFactory<String, OrderDto> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean(value = "kafkaTemplate")
    public KafkaTemplate<String, OrderDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


 */
}
