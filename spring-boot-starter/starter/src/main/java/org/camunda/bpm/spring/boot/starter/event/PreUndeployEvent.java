package org.camunda.bpm.spring.boot.starter.event;

import org.camunda.bpm.engine.ProcessEngine;

public class PreUndeployEvent {

  private final ProcessEngine processEngine;


  public PreUndeployEvent(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  @Override
  public String toString() {
    return "PreUndeployEvent{" +
      "processEngine=" + processEngine +
      '}';
  }
}

