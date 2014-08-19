package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.engine.delegate.PersistentVariableScope;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

public class SetExecutionVariableFromSerializedCmd extends SetScopeVariableFromSerializedCmd {

  public SetExecutionVariableFromSerializedCmd(String executionId, String variableName, Object serializedVariableValue, String variableType,
      Map<String, Object> configuration, boolean isLocal) {
    super(executionId, variableName, serializedVariableValue, variableType, configuration, isLocal);
  }

  protected PersistentVariableScope getPersistentVariableScope(CommandContext commandContext) {
    ensureNotNull("executionId", scopeId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(scopeId);

    ensureNotNull("execution " + scopeId + " doesn't exist", "execution", execution);

    return execution;
  }

}
