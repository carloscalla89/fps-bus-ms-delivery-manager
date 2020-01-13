package com.inretailpharma.digital.deliverymanager.service.impl;


import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.service.IPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service("kafkaPublisherService")
public class KafkaPublisherServiceImpl implements IPublisherService {
    @Override
    public void sendOrder(OrderDto orderDto) {

    }

    @Override
    public void sendOrderCallBack(OrderDto orderDto) {

    }
/*
    @Value("${spring.kafka.producer.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, OrderDto> kafkaTemplate;

    @Override
    public void sendOrder(OrderDto orderDto) {
        log.info("Starting send topic:"+orderDto);
        ListenableFuture<SendResult<String , OrderDto>> future = kafkaTemplate.send(topic, orderDto);

        future.addCallback(new ListenableFutureCallback<SendResult<String, OrderDto>>() {

            @Override
            public void onSuccess(SendResult<String, OrderDto> result) {

                log.info("On success:"+result);
            }

            @Override
            public void onFailure(Throwable ex) {
                ex.printStackTrace();
                log.error("error:"+ex);
            }

        });
    }

    @Override
    public void sendOrderCallBack(OrderDto orderDto) {

    }

 */
}
