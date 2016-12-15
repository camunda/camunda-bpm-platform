package org.camunda.bpm.integrationtest.functional.classloading.beans;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import javax.inject.Named;

@Named(value = "taskListener")
public class ExampleTaskListener implements TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {
    delegateTask.setVariable("listener", "listener-notified");
  }

}
