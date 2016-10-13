package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveExecutionVariablesCmd extends AbstractRemoveVariableCmd {

  private static final long serialVersionUID = 1L;

  public RemoveExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    super(executionId, variableNames, isLocal);
  }

  protected ExecutionEntity getEntity() {
    ensureNotNull("executionId", entityId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(entityId);

    ensureNotNull("execution " + entityId + " doesn't exist", "execution", execution);

    checkRemoveExecutionVariables(execution);

    return execution;
  }

  @Override
  protected ExecutionEntity getContextExecution() {
    return getEntity();
  }

  protected void logVariableOperation(AbstractVariableScope scope) {
    ExecutionEntity execution = (ExecutionEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution.getId(), null, PropertyChange.EMPTY_CHANGE);
  }

  protected void checkRemoveExecutionVariables(ExecutionEntity execution) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }
  }
}
