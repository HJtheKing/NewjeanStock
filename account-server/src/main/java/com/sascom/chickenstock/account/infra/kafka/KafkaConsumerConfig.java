package com.sascom.chickenstock.account.infra.kafka;
import com.sascom.chickenstock.account.api.dto.PortfolioCompensationEvent; import org.apache.kafka.clients.consumer.ConsumerConfig; import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory; import org.springframework.kafka.core.*; import org.springframework.kafka.support.serializer.JsonDeserializer; import java.util.*;
@Configuration public class KafkaConsumerConfig {
  @Value("${spring.kafka.bootstrap-servers}") private String bootstrap;
  @Bean public ConsumerFactory<String, PortfolioCompensationEvent> portfolioCompConsumerFactory(){ Map<String,Object> props=new HashMap<>(); props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap); props.put(ConsumerConfig.GROUP_ID_CONFIG,"account-service"); props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest"); JsonDeserializer<PortfolioCompensationEvent> jd=new JsonDeserializer<>(PortfolioCompensationEvent.class); jd.addTrustedPackages("*"); return new DefaultKafkaConsumerFactory<>(props,new StringDeserializer(), jd); }
  @Bean public ConcurrentKafkaListenerContainerFactory<String, PortfolioCompensationEvent> portfolioCompKafkaFactory(){ var f=new ConcurrentKafkaListenerContainerFactory<String, PortfolioCompensationEvent>(); f.setConsumerFactory(portfolioCompConsumerFactory()); return f; }
}
