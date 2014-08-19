package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.engine.delegate.PersistentVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public abstract class SetScopeVariableFromSerializedCmd implements Command<Void> {

  protected boolean isLocal;
  protected String variableName;
  protected Object serializedVariableValue;
  protected String variableType;
  protected Map<String, Object> configuration;
  protected String scopeId;

  public SetScopeVariableFromSerializedCmd(String scopeId, String variableName, Object serializedVariableValue,
      String variableType, Map<String, Object> configuration, boolean isLocal) {
    this.isLocal = isLocal;
    this.variableName = variableName;
    this.serializedVariableValue = serializedVariableValue;
    this.variableType = variableType;
    this.configuration = configuration;
    this.scopeId = scopeId;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("variableType", variableType);

    PersistentVariableScope scope = getPersistentVariableScope(commandContext);

    if (isLocal) {
      scope.setVariableLocalFromSerialized(variableName, serializedVariableValue, variableType, configuration);
    } else {
      scope.setVariableFromSerialized(variableName, serializedVariableValue, variableType, configuration);
    }

    return null;
  }

  protected abstract PersistentVariableScope getPersistentVariableScope(CommandContext commandContext);
}
