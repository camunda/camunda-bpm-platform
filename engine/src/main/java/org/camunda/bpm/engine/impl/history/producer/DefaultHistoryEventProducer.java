/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.producer;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultHistoryEventProducer implements HistoryEventProducer {
  
  protected void initHistoryEvent(HistoryEvent evt, String eventType) {
    
    final IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();

    Date currentTime = ClockUtil.getCurrentTime();

    evt.setEventType(eventType);   
    evt.setTimestamp(currentTime);
    evt.setId(idGenerator.getNextId());
    
  }
  
  protected void initHistoryEvent(HistoryEvent evt, ExecutionEntity execution, String eventType) {
    
    initHistoryEvent(evt, eventType);

    String processDefinitionId = execution.getProcessDefinitionId();
    String processInstanceId = execution.getProcessInstanceId();
    String executionId = execution.getId();

    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);
    
  }
  
  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt, ExecutionEntity execution, String eventType) {
    
    // call common init behavior
    initHistoryEvent(evt, execution, eventType);
    
    String activityId = execution.getActivityId();
    String activityInstanceId = execution.getActivityInstanceId();
    String parentActivityInstanceId = execution.getParentActivityInstanceId();
    
    evt.setActivityInstanceId(activityInstanceId);
    evt.setParentActivityInstanceId(parentActivityInstanceId);

    PvmScope eventSource = null;
    if(activityId != null) {
      eventSource = execution.getActivity();
    } else {
      eventSource = (PvmScope) execution.getEventSource(); 
    }

    evt.setActivityId(eventSource.getId());
    evt.setActivityName((String) eventSource.getProperty("name"));
    evt.setActivityType((String) eventSource.getProperty("type"));
        
  }
  
  protected void initProcessInstanceEvent(HistoricProcessInstanceEventEntity evt, ExecutionEntity execution, String eventType) {
    
    // call common init behavior
    initHistoryEvent(evt, execution, eventType);
    
    // set activity id
    evt.setActivityId(execution.getActivityId());
    
    // set business key
    evt.setBusinessKey(execution.getProcessBusinessKey());
        
  }
  
  protected void initTaskInstanceEvent(HistoricTaskInstanceEventEntity evt, TaskEntity taskEntity, String eventType) {
    
    initHistoryEvent(evt, eventType);
    
    String processDefinitionId = taskEntity.getProcessDefinitionId();
    String processInstanceId = taskEntity.getProcessInstanceId();
    String executionId = taskEntity.getExecutionId();

    evt.setTaskId(taskEntity.getId());
    
    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);
    
    evt.setAssignee(taskEntity.getAssignee());
    evt.setDescription(taskEntity.getDescription());
    evt.setDueDate(taskEntity.getDueDate());
    evt.setName(taskEntity.getName());
    evt.setOwner(taskEntity.getOwner());
    evt.setParentTaskId(taskEntity.getParentTaskId());
    evt.setPriority(taskEntity.getPriority());
    evt.setTaskDefinitionKey(taskEntity.getTaskDefinitionKey());
    
  }
  
  // Implementation ////////////////////////////////

  public HistoryEvent createProcessInstanceStartEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    // create event instance
    HistoricProcessInstanceEventEntity evt = new HistoricProcessInstanceEventEntity();
       
    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEvent.ACTIVITY_EVENT_TYPE_START);
    
    // set super process instance id
    ExecutionEntity superExecution = executionEntity.getSuperExecution();
    if (superExecution != null) {
      evt.setSuperProcessInstanceId(superExecution.getProcessInstanceId());
    }

    // set start user Id
    evt.setStartUserId(Authentication.getAuthenticatedUserId());
    
    return evt;
  }

  public HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    // create event instance
    HistoricProcessInstanceEventEntity evt = new HistoricProcessInstanceEventEntity();
       
    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEvent.ACTIVITY_EVENT_TYPE_END);
    
    // set delete reason (if applicable).
    if (executionEntity.getDeleteReason() != null) {
      evt.setDeleteReason(executionEntity.getDeleteReason());
    }
    
    return evt;
  }

  public HistoryEvent createActivityInstanceStartEvt(DelegateExecution execution) {
    
    // create event instance
    HistoricActivityInstanceEventEntity evt = new HistoricActivityInstanceEventEntity();
       
    // initialize event
    initActivityInstanceEvent(evt, (ExecutionEntity) execution, HistoryEvent.ACTIVITY_EVENT_TYPE_START);
        
    return evt;
  }
  
  public HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution, DelegateTask task) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    // create event instance
    HistoricActivityInstanceEventEntity evt = new HistoricActivityInstanceEventEntity();
       
    // initialize event
    initActivityInstanceEvent(evt, executionEntity, HistoryEvent.ACTIVITY_EVENT_TYPE_UPDATE);
    
    // update task assignment
    if(task != null) {
      evt.setTaskId(task.getId());
      evt.setTaskAssignee(task.getAssignee());
    }
    
    // update sub process reference
    ExecutionEntity subProcessInstance = executionEntity.getSubProcessInstance();
    if (subProcessInstance != null) {
      evt.setCalledProcessInstanceId(subProcessInstance.getId());
    }
        
    return evt;
  }
  
  public HistoryEvent createActivityInstanceEndEvt(DelegateExecution execution) {
    
    // create event instance
    HistoricActivityInstanceEventEntity evt = new HistoricActivityInstanceEventEntity();
       
    // initialize event
    initActivityInstanceEvent(evt, (ExecutionEntity) execution, HistoryEvent.ACTIVITY_EVENT_TYPE_END);
        
    return evt;
  }
  
  public HistoryEvent createTaskInstanceCreateEvt(DelegateTask task) {
    
    // create event instance
    HistoricTaskInstanceEventEntity evt = new HistoricTaskInstanceEventEntity();
       
    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_CREATE);
        
    return evt;
  }
  
  public HistoryEvent createTaskInstanceUpdateEvt(DelegateTask task) {
    
    // create event instance
    HistoricTaskInstanceEventEntity evt = new HistoricTaskInstanceEventEntity();
       
    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_UPDATE);
        
    return evt;
  }
  
  public HistoryEvent createTaskInstanceCompleteEvt(DelegateTask task, String deleteReason) {
    
    // create event instance
    HistoricTaskInstanceEventEntity evt = new HistoricTaskInstanceEventEntity();
       
    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_DELETE);
    
    // set delete reason
    evt.setDeleteReason(deleteReason);
        
    return evt;
  }


}
