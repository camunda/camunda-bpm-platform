package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.Command;

public interface FailedJobCommandFactory {
	
	public Command<Object> getCommand(String jobId, Throwable exception);

}
