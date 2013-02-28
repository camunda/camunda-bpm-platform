package org.activiti.engine.test.api.mgmt;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    Boolean fail = (Boolean) execution.getVariable("fail");
    if (fail != false) {
      throw new ActivitiException("Expected exception");
    }
  }

}
