package org.camunda.bpm.engine.impl.bpmn.delegate;

import org.camunda.bpm.engine.delegate.ConditionDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.delegate.DelegateInvocation;

public class ConditionDelegateInvocation extends DelegateInvocation {

    protected final ConditionDelegate delegateInstance;
    protected final DelegateExecution execution;

    public ConditionDelegateInvocation(ConditionDelegate delegateInstance, DelegateExecution execution) {
        super(execution, null);
        this.delegateInstance = delegateInstance;
        this.execution = execution;
    }

    protected void invoke() throws Exception {
        invocationResult = delegateInstance.evaluate(execution);
    }

}