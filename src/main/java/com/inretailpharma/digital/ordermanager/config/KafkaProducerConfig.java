package com.inretailpharma.digital.ordermanager.config;


import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaServers;

    /*
    @Value("${spring.kafka.registry.url}")
    private String registryUrl;

    @Value("${spring.kafka.registry.api-key}")
    private String registryApiKey;

    @Value("${spring.kafka.registry.api-secret}")
    private String registryApiSecret;

     */
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
        //props.put("schema.registry.basic.auth.user.info",String.format("%s:%s",registryApiKey,registryApiSecret));
        //props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,registryUrl);
        props.put("retries", 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaJsonSerializer");
        //props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");


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

}
