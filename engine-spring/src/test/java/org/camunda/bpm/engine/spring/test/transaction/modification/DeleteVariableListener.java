package org.camunda.bpm.engine.spring.test.transaction.modification;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class DeleteVariableListener implements ExecutionListener {
    public void notify(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.removeVariable("createDate");
    }
}
