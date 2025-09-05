package com.sascom.chickenstock.account.infra.kafka;
import com.sascom.chickenstock.account.api.dto.PortfolioCompensationEvent; import com.sascom.chickenstock.account.domain.service.AccountCompensationService; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import org.springframework.beans.factory.annotation.Value; import org.springframework.kafka.annotation.KafkaListener; import org.springframework.stereotype.Component;
@Slf4j @Component @RequiredArgsConstructor public class PortfolioCompensationConsumer {
  private final AccountCompensationService service; @Value("${topics.portfolio-compensate}") private String topicName;
  @KafkaListener(topics="#{__listener.topicName}", containerFactory="portfolioCompKafkaFactory") public void onMessage(PortfolioCompensationEvent e){ log.warn("[KAFKA] portfolio compensate: {}", e); service.compensate(e); }
}
