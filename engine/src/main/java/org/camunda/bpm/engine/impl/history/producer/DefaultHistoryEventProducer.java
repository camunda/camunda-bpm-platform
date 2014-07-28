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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.IncidentState;
import org.camunda.bpm.engine.history.UserOperationLogContext;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultHistoryEventProducer implements HistoryEventProducer {

  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt, ExecutionEntity execution, String eventType) {

    String activityId = execution.getActivityId();
    String activityInstanceId = execution.getActivityInstanceId();
    String parentActivityInstanceId = execution.getParentActivityInstanceId();

    evt.setId(activityInstanceId);
    evt.setEventType(eventType);
    evt.setActivityInstanceId(activityInstanceId);
    evt.setParentActivityInstanceId(parentActivityInstanceId);
    evt.setProcessDefinitionId(execution.getProcessDefinitionId());
    evt.setProcessInstanceId(execution.getProcessInstanceId());
    evt.setExecutionId(execution.getId());

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

    String processDefinitionId = execution.getProcessDefinitionId();
    String processInstanceId = execution.getProcessInstanceId();
    String executionId = execution.getId();

    evt.setId(processInstanceId);
    evt.setEventType(eventType);
    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);
    evt.setBusinessKey(execution.getProcessBusinessKey());

  }

  protected void initTaskInstanceEvent(HistoricTaskInstanceEventEntity evt, TaskEntity taskEntity, String eventType) {

    String processDefinitionId = taskEntity.getProcessDefinitionId();
    String processInstanceId = taskEntity.getProcessInstanceId();
    String executionId = taskEntity.getExecutionId();

    evt.setId(taskEntity.getId());
    evt.setEventType(eventType);
    evt.setTaskId(taskEntity.getId());

    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);

    evt.setAssignee(taskEntity.getAssignee());
    evt.setDescription(taskEntity.getDescription());
    evt.setDueDate(taskEntity.getDueDate());
    evt.setFollowUpDate(taskEntity.getFollowUpDate());
    evt.setName(taskEntity.getName());
    evt.setOwner(taskEntity.getOwner());
    evt.setParentTaskId(taskEntity.getParentTaskId());
    evt.setPriority(taskEntity.getPriority());
    evt.setTaskDefinitionKey(taskEntity.getTaskDefinitionKey());

    ExecutionEntity execution = (ExecutionEntity) taskEntity.getExecution();
    if (execution != null) {
      evt.setActivityInstanceId(execution.getActivityInstanceId());
    }

  }

  protected void initHistoricVariableUpdateEvt(HistoricVariableUpdateEventEntity evt, VariableInstanceEntity variableInstance, String eventType) {

    // init properties
    evt.setEventType(eventType);
    evt.setTimestamp(ClockUtil.getCurrentTime());
    evt.setVariableInstanceId(variableInstance.getId());
    evt.setProcessInstanceId(variableInstance.getProcessInstanceId());
    evt.setExecutionId(variableInstance.getExecutionId());
    evt.setTaskId(variableInstance.getTaskId());
    evt.setRevision(variableInstance.getRevision());
    evt.setVariableName(variableInstance.getName());
    evt.setVariableTypeName(variableInstance.getType().getTypeName());
    evt.setDataFormatId(variableInstance.getDataFormatId());
    evt.setConfiguration(variableInstance.getConfiguration());

    // copy value
    evt.setTextValue(variableInstance.getTextValue());
    evt.setTextValue2(variableInstance.getTextValue2());
    evt.setDoubleValue(variableInstance.getDoubleValue());
    evt.setLongValue(variableInstance.getLongValue());
    if (variableInstance.getByteArrayValueId() != null) {
      ByteArrayEntity byteArrayValue = variableInstance.getByteArrayValue();
      evt.setByteValue(byteArrayValue.getBytes());
    }
  }

  protected void initUserOperationLogEvent(UserOperationLogEntryEventEntity evt, UserOperationLogContext context, PropertyChange propertyChange) {
    // init properties
    evt.setEntityType(context.getEntityType());
    evt.setOperationType(context.getOperationType());
    evt.setOperationId(context.getOperationId());
    evt.setUserId(context.getUserId());
    evt.setProcessDefinitionId(context.getProcessDefinitionId());
    evt.setProcessInstanceId(context.getProcessInstanceId());
    evt.setExecutionId(context.getExecutionId());
    evt.setCaseDefinitionId(context.getCaseDefinitionId());
    evt.setCaseInstanceId(context.getCaseInstanceId());
    evt.setCaseExecutionId(context.getCaseExecutionId());
    evt.setTaskId(context.getTaskId());
    evt.setTimestamp(ClockUtil.getCurrentTime());

    // init property value
    evt.setProperty(propertyChange.getPropertyName());
    evt.setOrgValue(propertyChange.getOrgValueString());
    evt.setNewValue(propertyChange.getNewValueString());
  }

  protected void initHistoricIncidentEvent(HistoricIncidentEventEntity evt, Incident incident, String eventType) {
    // init properties
    evt.setId(incident.getId());
    evt.setProcessDefinitionId(incident.getProcessDefinitionId());
    evt.setProcessInstanceId(incident.getProcessInstanceId());
    evt.setExecutionId(incident.getExecutionId());
    evt.setCreateTime(incident.getIncidentTimestamp());
    evt.setIncidentType(incident.getIncidentType());
    evt.setActivityId(incident.getActivityId());
    evt.setCauseIncidentId(incident.getCauseIncidentId());
    evt.setRootCauseIncidentId(incident.getRootCauseIncidentId());
    evt.setConfiguration(incident.getConfiguration());
    evt.setIncidentMessage(incident.getIncidentMessage());

    // init event type
    evt.setEventType(eventType);

    // init state
    IncidentState incidentState = IncidentState.DEFAULT;
    if (HistoryEvent.INCIDENT_DELETE.equals(eventType)) {
      incidentState = IncidentState.DELETED;
    } else if (HistoryEvent.INCIDENT_RESOLVE.equals(eventType)) {
      incidentState = IncidentState.RESOLVED;
    }
    evt.setIncidentState(incidentState.getStateCode());
  }


  protected HistoryEvent createHistoricVariableEvent(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope, String eventType) {
    String scopeActivityInstanceId = null;
    String sourceActivityInstanceId = null;

    if(variableInstance.getExecutionId() != null) {
      ExecutionEntity scopeExecution = Context.getCommandContext()
        .getDbSqlSession()
        .selectById(ExecutionEntity.class, variableInstance.getExecutionId());

      if (variableInstance.getTaskId() == null
          && scopeExecution.isScope()
          && (scopeExecution.isActive() || (!scopeExecution.isActive() && scopeExecution.getActivityId() == null))) {
        scopeActivityInstanceId = scopeExecution.getParentActivityInstanceId();

      } else {
        scopeActivityInstanceId = scopeExecution.getActivityInstanceId();
      }
    }

    ExecutionEntity sourceExecution = null;
    if (sourceVariableScope instanceof ExecutionEntity) {
      sourceExecution = (ExecutionEntity) sourceVariableScope;
      sourceActivityInstanceId = sourceExecution.getActivityInstanceId();

    } else if (sourceVariableScope instanceof TaskEntity) {
      sourceExecution = ((TaskEntity) sourceVariableScope).getExecution();
      if (sourceExecution != null) {
        sourceActivityInstanceId = sourceExecution.getActivityInstanceId();
      }
    }

    // create event
    HistoricVariableUpdateEventEntity evt = newVariableUpdateEventEntity(sourceExecution);
    // initialize
    initHistoricVariableUpdateEvt(evt, variableInstance, eventType);

    // set scope activity instance id
    evt.setScopeActivityInstanceId(scopeActivityInstanceId);
    // set source activity instance id
    evt.setActivityInstanceId(sourceActivityInstanceId);

    return evt;
  }

  // event instance factory ////////////////////////

  protected HistoricProcessInstanceEventEntity newProcessInstanceEventEntity(ExecutionEntity execution) {
    return new HistoricProcessInstanceEventEntity();
  }

  protected HistoricActivityInstanceEventEntity newActivityInstanceEventEntity(ExecutionEntity execution) {
    return new HistoricActivityInstanceEventEntity();
  }

  protected HistoricTaskInstanceEventEntity newTaskInstanceEventEntity(DelegateTask task) {
    return new HistoricTaskInstanceEventEntity();
  }

  protected HistoricVariableUpdateEventEntity newVariableUpdateEventEntity(ExecutionEntity execution) {
    return new HistoricVariableUpdateEventEntity();
  }

  protected HistoricFormPropertyEventEntity newHistoricFormPropertyEvent() {
    return new HistoricFormPropertyEventEntity();
  }

  protected HistoricIncidentEventEntity newIncidentEventEntity(Incident incident) {
    return new HistoricIncidentEventEntity();
  }

  protected HistoricProcessInstanceEventEntity loadProcessInstanceEventEntity(ExecutionEntity execution) {
    return newProcessInstanceEventEntity(execution);
  }

  protected HistoricActivityInstanceEventEntity loadActivityInstanceEventEntity(ExecutionEntity execution) {
    return newActivityInstanceEventEntity(execution);
  }

  protected HistoricTaskInstanceEventEntity loadTaskInstanceEvent(DelegateTask task) {
    return newTaskInstanceEventEntity(task);
  }

  protected HistoricIncidentEventEntity loadIncidentEvent(Incident incident) {
    return newIncidentEventEntity(incident);
  }

  // Implementation ////////////////////////////////

  public HistoryEvent createProcessInstanceStartEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = newProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEvent.ACTIVITY_EVENT_TYPE_START);

    evt.setStartActivityId(executionEntity.getActivityId());
    evt.setStartTime(ClockUtil.getCurrentTime());

    // set super process instance id
    ExecutionEntity superExecution = executionEntity.getSuperExecution();
    if (superExecution != null) {
      evt.setSuperProcessInstanceId(superExecution.getProcessInstanceId());
    }

    // set start user Id
    evt.setStartUserId(Context.getCommandContext().getAuthenticatedUserId());

    return evt;
  }

  public HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = loadProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEvent.ACTIVITY_EVENT_TYPE_END);

    // set end activity id
    evt.setEndActivityId(executionEntity.getActivityId());
    evt.setEndTime(ClockUtil.getCurrentTime());

    if(evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime()-evt.getStartTime().getTime());
    }

    // set delete reason (if applicable).
    if (executionEntity.getDeleteReason() != null) {
      evt.setDeleteReason(executionEntity.getDeleteReason());
    }

    return evt;
  }

  public HistoryEvent createActivityInstanceStartEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricActivityInstanceEventEntity evt = newActivityInstanceEventEntity(executionEntity);

    // initialize event
    initActivityInstanceEvent(evt, (ExecutionEntity) execution, HistoryEvent.ACTIVITY_EVENT_TYPE_START);

    evt.setStartTime(ClockUtil.getCurrentTime());

    return evt;
  }

  public HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution, DelegateTask task) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricActivityInstanceEventEntity evt = loadActivityInstanceEventEntity(executionEntity);

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
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricActivityInstanceEventEntity evt = loadActivityInstanceEventEntity(executionEntity);
    evt.setActivityInstanceState(executionEntity.getActivityInstanceState());

    // initialize event
    initActivityInstanceEvent(evt, (ExecutionEntity) execution, HistoryEvent.ACTIVITY_EVENT_TYPE_END);

    evt.setEndTime(ClockUtil.getCurrentTime());
    if(evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime()-evt.getStartTime().getTime());
    }

    return evt;
  }

  public HistoryEvent createTaskInstanceCreateEvt(DelegateTask task) {

    // create event instance
    HistoricTaskInstanceEventEntity evt = newTaskInstanceEventEntity(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_CREATE);

    evt.setStartTime(ClockUtil.getCurrentTime());

    return evt;
  }

  public HistoryEvent createTaskInstanceUpdateEvt(DelegateTask task) {

    // create event instance
    HistoricTaskInstanceEventEntity evt = loadTaskInstanceEvent(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_UPDATE);

    return evt;
  }

  public HistoryEvent createTaskInstanceCompleteEvt(DelegateTask task, String deleteReason) {

    // create event instance
    HistoricTaskInstanceEventEntity evt = loadTaskInstanceEvent(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEvent.TASK_EVENT_TYPE_DELETE);

    // set end time
    evt.setEndTime(ClockUtil.getCurrentTime());
    if(evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime()-evt.getStartTime().getTime());
    }

    // set delete reason
    evt.setDeleteReason(deleteReason);

    return evt;
  }

  // User Operation Logs ///////////////////////////

  public List<HistoryEvent> createUserOperationLogEvents(UserOperationLogContext context) {
    List<HistoryEvent> historyEvents = new ArrayList<HistoryEvent>();

    String userId = null;
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      userId = commandContext.getAuthenticatedUserId();
    }
    context.setUserId(userId);

    String operationId = Context.getProcessEngineConfiguration().getIdGenerator().getNextId();
    context.setOperationId(operationId);

    for (PropertyChange propertyChange : context.getPropertyChanges()) {
      UserOperationLogEntryEventEntity evt = new UserOperationLogEntryEventEntity();

      initUserOperationLogEvent(evt, context, propertyChange);

      historyEvents.add(evt);
    }

    return historyEvents;
  }

  // variables /////////////////////////////////

  public HistoryEvent createHistoricVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEvent.VARIABLE_EVENT_TYPE_CREATE);
  }

  public HistoryEvent createHistoricVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEvent.VARIABLE_EVENT_TYPE_DELETE);
  }

  public HistoryEvent createHistoricVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEvent.VARIABLE_EVENT_TYPE_UPDATE);
  }

  // form Properties ///////////////////////////

  public HistoryEvent createFormPropertyUpdateEvt(ExecutionEntity execution, String propertyId, Object propertyValue, String taskId) {

    final IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();

    HistoricFormPropertyEventEntity historicFormPropertyEntity = newHistoricFormPropertyEvent();

    historicFormPropertyEntity.setId(idGenerator.getNextId());
    historicFormPropertyEntity.setEventType(HistoryEvent.FORM_PROPERTY_UPDATE);
    historicFormPropertyEntity.setTimestamp(ClockUtil.getCurrentTime());
    historicFormPropertyEntity.setActivityInstanceId(execution.getActivityInstanceId());
    historicFormPropertyEntity.setExecutionId(execution.getId());
    historicFormPropertyEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    historicFormPropertyEntity.setProcessInstanceId(execution.getProcessInstanceId());
    historicFormPropertyEntity.setPropertyId(propertyId);
    historicFormPropertyEntity.setPropertyValue(propertyValue);
    historicFormPropertyEntity.setTaskId(taskId);

    return historicFormPropertyEntity;
  }

  // Incidents //////////////////////////////////

  public HistoryEvent createHistoricIncidentCreateEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEvent.INCIDENT_CREATE);
  }

  public HistoryEvent createHistoricIncidentResolveEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEvent.INCIDENT_RESOLVE);
  }

  public HistoryEvent createHistoricIncidentDeleteEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEvent.INCIDENT_DELETE);
  }

  protected HistoryEvent createHistoricIncidentEvt(Incident incident, String eventType) {
    // create event
    HistoricIncidentEventEntity evt = loadIncidentEvent(incident);
    // initialize
    initHistoricIncidentEvent(evt, incident, eventType);

    if (!HistoryEvent.INCIDENT_CREATE.equals(eventType)) {
      evt.setEndTime(ClockUtil.getCurrentTime());
    }

    return evt;
  }

}
