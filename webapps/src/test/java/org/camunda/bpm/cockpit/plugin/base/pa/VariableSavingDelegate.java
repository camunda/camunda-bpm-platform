package org.camunda.bpm.cockpit.plugin.base.pa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class VariableSavingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    execution.setVariable("varstring", "FOO");
    execution.setVariable("varstring2", "F_OO");
    execution.setVariable("varinteger", 12);
    execution.setVariable("varfloat", 12.12);
    execution.setVariable("varboolean", true);
  }
}
