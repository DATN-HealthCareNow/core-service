package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

  private final RabbitTemplate rabbitTemplate;

  // We explicitly send to the main topic exchange routing key for
  // notification.queue
  private static final String EXCHANGE_NAME = "healthcare.events";
  private static final String ROUTING_KEY = "notification.event";

  public void sendNotification(NotificationEvent event) {
    log.info("Sending notification event: {}", event.getEventType());
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
  }
}
