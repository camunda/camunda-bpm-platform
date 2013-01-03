package com.camunda.fox.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


public class FoxDeleteProcessInstanceCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String deleteReason;

  public FoxDeleteProcessInstanceCmd(String processInstanceId, String deleteReason) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
  }

  public Void execute(CommandContext commandContext) { 
    if(processInstanceId == null) {
      throw new ActivitiException("processInstanceId is null");
    }
    
    ExecutionEntity execution = commandContext
                                  .getExecutionManager()
                                  .findExecutionById(processInstanceId);
    
    if(execution == null) {
      throw new ActivitiException("No process instance found for id '" + processInstanceId + "'");
    }
    
    commandContext
      .getTaskManager()
      .deleteTasksByProcessInstanceId(processInstanceId, deleteReason, false);

    for (InterpretableExecution currentExecution : this.collectExecutionToDelete(execution)) {
      currentExecution.deleteCascade2(deleteReason);
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public List<InterpretableExecution> collectExecutionToDelete(InterpretableExecution execution) {
    List<InterpretableExecution> result = new ArrayList<InterpretableExecution>();
    for (InterpretableExecution currentExecution : (List<InterpretableExecution>) execution.getExecutions()) {
      result.addAll(this.collectExecutionToDelete(currentExecution));
    }
    if (execution.getSubProcessInstance() != null) {
      result.addAll(this.collectExecutionToDelete(execution.getSubProcessInstance()));
    }
    result.add(execution);
    return result;
  }

}
