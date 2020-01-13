package com.inretailpharma.digital.deliverymanager.config;

import org.springframework.context.annotation.Configuration;


@Configuration
public class KafkaConsumerConfig {
/*
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String kafkaServers;

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
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order_callback_topic");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderCallBackDeserializer.class.getName());

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
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.setConcurrency(3);

        return factory;
    }


 */
}
