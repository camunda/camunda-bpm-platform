package com.camunda.fox.platform.test.functional.transactions;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class UpdateRouterConfiguration implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    
    String version = (String) execution.getVariable("version");
    if ("1.0".equals(version) || "2.0".equals(version)) {
      System.out.println(" ### Updating router configuration..." );
    } else {
      throw new Exception("Unsupported Version: " + version);
    }
  }

}
