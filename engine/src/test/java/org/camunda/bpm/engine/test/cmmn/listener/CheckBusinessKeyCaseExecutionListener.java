package org.camunda.bpm.engine.test.cmmn.listener;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;

public class CheckBusinessKeyCaseExecutionListener implements CaseExecutionListener {

  @Override
  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    caseExecution.setVariable("businessKey", caseExecution.getBusinessKey());
    caseExecution.setVariable("caseBusinessKey", caseExecution.getCaseBusinessKey());
  }

}
