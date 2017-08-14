package org.camunda.bpm.spring.boot.starter.event;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public class ProcessApplicationEventPublisher implements ApplicationContextAware {

  private final ApplicationEventPublisher publisher;
  private ApplicationContext parentContext;

  public ProcessApplicationEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @EventListener
  public void handleApplicationReadyEvent(final ApplicationReadyEvent applicationReadyEvent) {
    publisher.publishEvent(new ProcessApplicationStartedEvent(applicationReadyEvent));
  }

  @EventListener
  public void handleContextStoppedEvent(final ContextClosedEvent contextStoppedEvent) {
    if (parentContext == contextStoppedEvent.getApplicationContext()) {
      publisher.publishEvent(new ProcessApplicationStoppedEvent(contextStoppedEvent));
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.parentContext = applicationContext;
  }
}
