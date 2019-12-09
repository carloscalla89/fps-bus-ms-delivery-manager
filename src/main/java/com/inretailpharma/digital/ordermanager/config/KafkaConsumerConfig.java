package com.inretailpharma.digital.ordermanager.config;


import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.*;

@Configuration
public class KafkaConsumerConfig {

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
    public Map<String, Object> consumerConfigs() {
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
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class.getName());



        return props;
    }

    @Bean
    public ConsumerFactory<String, OrderDto> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OrderDto>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String,  OrderDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);

        return factory;
    }

}
