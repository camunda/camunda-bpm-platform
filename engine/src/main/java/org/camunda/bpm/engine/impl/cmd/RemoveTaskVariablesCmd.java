package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveTaskVariablesCmd extends AbstractRemoveVariableCmd {

  private static final long serialVersionUID = 1L;

  public RemoveTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal) {
    super(taskId, variableNames, isLocal);
  }

  protected TaskEntity getEntity() {
    ensureNotNull("taskId", entityId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(entityId);

    ensureNotNull("Cannot find task with id " + entityId, "task", task);

    checkRemoveTaskVariables(task);

    return task;
  }

  @Override
  protected ExecutionEntity getContextExecution() {
    return getEntity().getExecution();
  }

  protected void logVariableOperation(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), null, task.getId(), PropertyChange.EMPTY_CHANGE);
  }

  protected void checkRemoveTaskVariables(TaskEntity task) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateTask(task);
    }
  }
}
