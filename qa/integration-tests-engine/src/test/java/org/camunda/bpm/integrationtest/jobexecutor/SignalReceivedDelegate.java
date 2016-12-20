package org.camunda.bpm.integrationtest.jobexecutor;

import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


@Named
public class SignalReceivedDelegate implements JavaDelegate {    
  
  @Inject
  private BusinessProcess businessProcess;
  
  public void execute(DelegateExecution execution) {
    businessProcess.setVariable("processName", "catchSignal-visited (was " + businessProcess.getVariable("processName")  + ")");
//    log.log(Level.INFO, "");
  }
}