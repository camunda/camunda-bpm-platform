package org.camunda.bpm.integrationtest.functional.classloading.beans;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

@Named(value = "taskListener")
public class ExampleTaskListener implements TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {
    delegateTask.setVariable("listener", "listener-notified");
  }

}
