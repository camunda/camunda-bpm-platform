package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveTaskVariablesCmd extends AbstractRemoveVariableCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;

  public RemoveTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal) {
    super(taskId, variableNames, isLocal);
  }

  @Override
  public void checkParameters() {
    ensureNotNull("taskId", entityId);
  }

  @Override
  public AbstractVariableScope getEntity() {
    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(entityId);

    ensureNotNull("Cannot find task with id " + entityId, "task", task);

    return task;
  }

  @Override
  public void logVariableOperation(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task, PropertyChange.EMPTY_CHANGE);
  }
}
