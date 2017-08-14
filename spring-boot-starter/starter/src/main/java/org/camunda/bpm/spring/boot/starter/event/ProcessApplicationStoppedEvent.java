package org.camunda.bpm.spring.boot.starter.event;

public class ProcessApplicationStoppedEvent extends ProcessApplicationEvent {

  private static final long serialVersionUID = 8052917038949847157L;

  public ProcessApplicationStoppedEvent(Object source) {
    super(source);
  }

}
