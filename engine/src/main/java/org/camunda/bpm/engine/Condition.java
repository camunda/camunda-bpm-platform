package org.camunda.bpm.engine;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public interface Condition {
	 public boolean shouldMap(DelegateExecution e);
}
