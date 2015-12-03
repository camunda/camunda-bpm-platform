package org.camunda.bpm.engine.test.api.cfg;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
  }

}
