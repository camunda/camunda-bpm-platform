package org.camunda.bpm.engine.impl.core.variable;

import java.util.Map;

import org.camunda.bpm.engine.delegate.PersistentVariableInstance;

public interface CorePersistentVariableStore extends CoreVariableStore<PersistentVariableInstance> {

  PersistentVariableInstance createVariableInstanceFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution);

  void createOrUpdateVariableFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution);
}
