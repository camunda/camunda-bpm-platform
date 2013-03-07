package org.camunda.bpm.engine.spring.test.components.jobexecutor;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * 
 * @author Pablo Ganga
 */
public class ForcedRollbackExecutionListener  implements ExecutionListener  {

	public void notify(DelegateExecution delegateExecution) throws Exception {
		throw new RuntimeException("Forcing transaction rollback");
	}

}
