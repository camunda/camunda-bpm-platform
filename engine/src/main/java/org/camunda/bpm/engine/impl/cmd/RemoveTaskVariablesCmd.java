package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveTaskVariablesCmd extends AbstractVariableCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;

  protected final String taskId;
  protected final Collection<String> variableNames;
  protected final boolean isLocal;

  public RemoveTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal) {
    this.taskId = taskId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }
  
  public Void execute(CommandContext commandContext) {

    ensureNotNull("taskId", taskId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    if (isLocal) {
      task.removeVariablesLocal(variableNames);
    } else {
      task.removeVariables(variableNames);
    }

    if(!preventLogUserOperation) {
      String processDefinitionKey = null;
      if(task.getExecution() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getExecution().getProcessDefinition()).getKey();
      } else if(task.getProcessInstance() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getProcessInstance().getProcessDefinition()).getKey();
      }

      commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task.getExecutionId(),
        task.getProcessInstanceId(), task.getProcessDefinitionId(), processDefinitionKey, PropertyChange.EMPTY_CHANGE);
    }

    return null;
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_REMOVE_TASK_VARIABLE;
  }
}
