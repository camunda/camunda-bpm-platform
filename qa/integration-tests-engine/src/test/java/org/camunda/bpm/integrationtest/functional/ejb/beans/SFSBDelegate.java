package org.camunda.bpm.integrationtest.functional.ejb.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Stateless;
import javax.inject.Named;


@Named("SFSBDelegate")
@Stateless
public class SFSBDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(SFSBDelegate.class.getName(), true);
  }

}