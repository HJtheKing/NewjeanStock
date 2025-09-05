package com.sascom.chickenstock.global.kafka.kafkaproducer;

import com.sascom.chickenstock.global.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {

    private final ProducerProperties producerProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPortfolioCompensation(PortfolioCompensationEvent event) {
        sendMessage(producerProperties.topic().get("portfolio-compensated"), event);
    }

    private <T> void sendMessage(String topic, T payload) {
        kafkaTemplate.send(topic, payload)
                        .whenComplete((res, ex) ->{
                            if(ex == null) log.info("[KAFKA] success topic={}, offset={}", topic, res.getRecordMetadata().offset());
                            else log.error("[KAFKA] failed topic={}, offset={}", topic, res.getRecordMetadata().offset());
                        });
    }

}