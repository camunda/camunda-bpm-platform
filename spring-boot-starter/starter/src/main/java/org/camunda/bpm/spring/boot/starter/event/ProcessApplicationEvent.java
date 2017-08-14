package org.camunda.bpm.spring.boot.starter.event;

import org.springframework.context.ApplicationEvent;

public class ProcessApplicationEvent extends ApplicationEvent {

  private static final long serialVersionUID = 6304748292253010152L;

  public ProcessApplicationEvent(Object source) {
    super(source);
  }

}
