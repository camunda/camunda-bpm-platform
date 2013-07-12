package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author roman.smirnov
 * @author Joram Barrez
 */
public class RemoveExecutionVariablesCmd implements Command<Void>, Serializable {

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
    if (executionId == null) {
      throw new ProcessEngineException("executionId is null");
    }
    
    ExecutionEntity execution = commandContext
            .getExecutionManager()
            .findExecutionById(executionId);
          
    if (execution==null) {
      throw new ProcessEngineException("execution "+executionId+" doesn't exist");
    }
    
    if (isLocal) {
      execution.removeVariablesLocal(variableNames);
    } else {
      execution.removeVariables(variableNames);
    }
    
    return null;
  }
}
