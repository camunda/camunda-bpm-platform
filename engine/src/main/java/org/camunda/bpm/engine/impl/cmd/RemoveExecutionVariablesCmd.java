package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveExecutionVariablesCmd extends AbstractVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private String executionId;
  private Collection<String> variableNames;
  private boolean isLocal;
  
  public RemoveExecutionVariablesCmd(String executionId, Collection<String> variableNames, boolean isLocal) {
    this.executionId = executionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }
  
  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);

    ensureNotNull("execution " + executionId + " doesn't exist", "execution", execution);

    if (isLocal) {
      execution.removeVariablesLocal(variableNames);
    } else {
      execution.removeVariables(variableNames);
    }

    if(!preventLogUserOperation) {
      String processDefinitionKey = ((ProcessDefinitionEntity) execution.getProcessDefinition()).getKey();
      commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution.getId(),
        execution.getProcessInstanceId(), execution.getProcessDefinitionId(), processDefinitionKey, PropertyChange.EMPTY_CHANGE);
    }

    return null;
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_REMOVE_EXECUTION_VARIABLE;
  }
}
