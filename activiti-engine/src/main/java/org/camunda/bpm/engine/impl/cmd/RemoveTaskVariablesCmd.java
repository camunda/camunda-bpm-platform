package org.camunda.bpm.engine.impl.cmd; 

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * @author roman.smirnov
 */
public class RemoveTaskVariablesCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;

  private final String taskId;
  private final Collection<String> variableNames;
  private final boolean isLocal;

  public RemoveTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal) {
    this.taskId = taskId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  public Void execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ProcessEngineException("taskId is null");
    }
    
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    
    if (task == null) {
      throw new ProcessEngineException("task "+taskId+" doesn't exist");
    }
    
    if (isLocal) {
      task.removeVariablesLocal(variableNames);
    } else {
      task.removeVariables(variableNames);
    }
    
    return null;
  }
}
