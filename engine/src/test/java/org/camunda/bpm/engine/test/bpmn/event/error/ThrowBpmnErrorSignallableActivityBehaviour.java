package org.camunda.bpm.engine.test.bpmn.event.error;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class ThrowBpmnErrorSignallableActivityBehaviour extends AbstractBpmnActivityBehavior {

  @Override
  public void execute(ActivityExecution execution) throws Exception {
  }
  
  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    throw new BpmnError("23", "Testing bpmn error in SignallableActivityBehaviour#signal");
  }
}
