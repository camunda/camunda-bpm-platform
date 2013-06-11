package org.camunda.bpm.engine.impl.history.handler.refactor;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;


public class UserTaskIdHandler implements TaskListener {

  public void notify(DelegateTask task) {
    TaskEntity t = (TaskEntity) task;
    ExecutionEntity execution = t.getExecution();
    if (execution != null) {
//      HistoricActivityInstanceEntity historicActivityInstance = ActivityInstanceEndHandler.findActivityInstance(execution);
//      historicActivityInstance.setTaskId(t.getId());
    }
  }
  
}
