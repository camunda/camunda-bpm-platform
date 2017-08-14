package org.camunda.bpm.spring.boot.starter.event;

import org.camunda.bpm.engine.ProcessEngine;

public class PostDeployEvent {

  private final ProcessEngine processEngine;

  public PostDeployEvent(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  @Override
  public String toString() {
    return "PostDeployEvent{" +
      "processEngine=" + processEngine +
      '}';
  }
}
