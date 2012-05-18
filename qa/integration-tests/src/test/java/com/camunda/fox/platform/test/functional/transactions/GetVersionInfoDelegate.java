package com.camunda.fox.platform.test.functional.transactions;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class GetVersionInfoDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    if ( !"23".equals(execution.getVariable("serialnumber"))) {
      throw new Exception( "The provided router serial number is wrong. The correct serial number is 23 ;-)" );
    }
    execution.setVariable("version", "2.0");
  }

}
