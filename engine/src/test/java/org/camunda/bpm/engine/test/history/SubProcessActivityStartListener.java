package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * @author Askar Akhmerov
 */
public class SubProcessActivityStartListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    Integer counter = (Integer) execution.getVariable("executionListenerCounter");
    if (counter == null) {
      counter = 0;
    }
    execution.setVariable("subExecutionListenerCounter", ++counter);
  }

}
