package org.camunda.bpm.integrationtest.functional.classloading.variables.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.junit.Assert;

public class GetDeserializableVariableDelegate implements JavaDelegate{

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    SerializableVariable variable = (SerializableVariable) execution.getVariable("var1");
  }
}
