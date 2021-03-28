package org.camunda.bpm.engine.delegate;

public interface ConditionDelegate {

    Boolean evaluate(DelegateExecution execution);

}