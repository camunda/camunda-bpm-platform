package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class IntermediateCatchLinkEventActivitiBehaviour extends IntermediateCatchEventActivitiBehaviour {

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    // a link event does not behave as a wait state (this is the only IntermediateCatchEvent behaving differently)
    leave(execution);
  }

}
