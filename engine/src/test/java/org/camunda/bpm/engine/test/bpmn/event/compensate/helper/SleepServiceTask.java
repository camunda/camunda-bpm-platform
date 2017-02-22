package org.camunda.bpm.engine.test.bpmn.event.compensate.helper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author Svetlana Dorokhova
 */
public class SleepServiceTask implements JavaDelegate {
  
  public void execute(DelegateExecution execution) throws Exception {
    //prevent same time instance execution
   Thread.sleep(1);
  }

}
