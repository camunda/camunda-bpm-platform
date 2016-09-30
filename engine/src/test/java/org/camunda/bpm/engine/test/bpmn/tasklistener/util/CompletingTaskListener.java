package org.camunda.bpm.engine.test.bpmn.tasklistener.util;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Askar Akhmerov
 */
public class CompletingTaskListener implements TaskListener {
  @Override
  public void notify(DelegateTask delegateTask) {
    delegateTask.complete();
  }
}
