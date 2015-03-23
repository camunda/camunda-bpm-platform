package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveExecutionVariablesCmd extends AbstractRemoveVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  public RemoveExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    super(executionId, variableNames, isLocal);
  }

  @Override
  public void checkParameters() {
    ensureNotNull("executionId", entityId);
  }

  @Override
  public AbstractVariableScope getEntity() {
    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(entityId);

    ensureNotNull("execution " + entityId + " doesn't exist", "execution", execution);

    return execution;
  }

  @Override
  public void logVariableOperation(AbstractVariableScope scope) {
    ExecutionEntity execution = (ExecutionEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution,
      PropertyChange.EMPTY_CHANGE);
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE;
  }
}
