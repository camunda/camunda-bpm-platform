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

import static org.camunda.bpm.engine.impl.util.JobExceptionUtil.createJobExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.JobExceptionUtil.getJobExceptionStacktrace;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.IncidentState;
import org.camunda.bpm.engine.history.JobState;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Daniel Meyer
 * @author Ingo Richtsmeier
 *
 */
public class DefaultHistoryEventProducer implements HistoryEventProducer {

  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt, ExecutionEntity execution, HistoryEventType eventType) {

    String activityId = execution.getActivityId();
    String activityInstanceId = execution.getActivityInstanceId();

    String parentActivityInstanceId = null;
    ExecutionEntity parentExecution = execution.getParent();

    if (parentExecution != null && CompensationBehavior.isCompensationThrowing(parentExecution)) {
      parentActivityInstanceId = CompensationBehavior.getParentActivityInstanceId(execution);
    }
    else {
      parentActivityInstanceId = execution.getParentActivityInstanceId();
    }

    evt.setId(activityInstanceId);
    evt.setEventType(eventType.getEventName());
    evt.setActivityInstanceId(activityInstanceId);
    evt.setParentActivityInstanceId(parentActivityInstanceId);
    evt.setProcessDefinitionId(execution.getProcessDefinitionId());
    evt.setProcessInstanceId(execution.getProcessInstanceId());
    evt.setExecutionId(execution.getId());

    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    if (definition != null) {
      evt.setProcessDefinitionKey(definition.getKey());
    }

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

  protected void initProcessInstanceEvent(HistoricProcessInstanceEventEntity evt, ExecutionEntity execution, HistoryEventType eventType) {

    String processDefinitionId = execution.getProcessDefinitionId();
    String processInstanceId = execution.getProcessInstanceId();
    String executionId = execution.getId();
    // the given execution is the process instance!
    String caseInstanceId = execution.getCaseInstanceId();

    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    String processDefinitionKey = null;
    if (definition != null) {
      processDefinitionKey = definition.getKey();
    }

    evt.setId(processInstanceId);
    evt.setEventType(eventType.getEventName());
    evt.setProcessDefinitionKey(processDefinitionKey);
    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);
    evt.setBusinessKey(execution.getProcessBusinessKey());
    evt.setCaseInstanceId(caseInstanceId);

    if (execution.getSuperCaseExecution() != null) {
      evt.setSuperCaseInstanceId(execution.getSuperCaseExecution().getCaseInstanceId());
    }
    if (execution.getSuperExecution() != null) {
      evt.setSuperProcessInstanceId(execution.getSuperExecution().getProcessInstanceId());
    }
  }

  protected void initTaskInstanceEvent(HistoricTaskInstanceEventEntity evt, TaskEntity taskEntity, HistoryEventType eventType) {

    String processDefinitionKey = null;
    ProcessDefinitionEntity definition = taskEntity.getProcessDefinition();
    if (definition != null) {
      processDefinitionKey = definition.getKey();
    }

    String processDefinitionId = taskEntity.getProcessDefinitionId();
    String processInstanceId = taskEntity.getProcessInstanceId();
    String executionId = taskEntity.getExecutionId();

    String caseDefinitionKey = null;
    CaseDefinitionEntity caseDefinition = taskEntity.getCaseDefinition();
    if (caseDefinition != null) {
      caseDefinitionKey = caseDefinition.getKey();
    }

    String caseDefinitionId = taskEntity.getCaseDefinitionId();
    String caseExecutionId = taskEntity.getCaseExecutionId();
    String caseInstanceId = taskEntity.getCaseInstanceId();

    evt.setId(taskEntity.getId());
    evt.setEventType(eventType.getEventName());
    evt.setTaskId(taskEntity.getId());

    evt.setProcessDefinitionKey(processDefinitionKey);
    evt.setProcessDefinitionId(processDefinitionId);
    evt.setProcessInstanceId(processInstanceId);
    evt.setExecutionId(executionId);

    evt.setCaseDefinitionKey(caseDefinitionKey);
    evt.setCaseDefinitionId(caseDefinitionId);
    evt.setCaseExecutionId(caseExecutionId);
    evt.setCaseInstanceId(caseInstanceId);

    evt.setAssignee(taskEntity.getAssignee());
    evt.setDescription(taskEntity.getDescription());
    evt.setDueDate(taskEntity.getDueDate());
    evt.setFollowUpDate(taskEntity.getFollowUpDate());
    evt.setName(taskEntity.getName());
    evt.setOwner(taskEntity.getOwner());
    evt.setParentTaskId(taskEntity.getParentTaskId());
    evt.setPriority(taskEntity.getPriority());
    evt.setTaskDefinitionKey(taskEntity.getTaskDefinitionKey());

    ExecutionEntity execution = taskEntity.getExecution();
    if (execution != null) {
      evt.setActivityInstanceId(execution.getActivityInstanceId());
    }

  }

  protected void initHistoricVariableUpdateEvt(HistoricVariableUpdateEventEntity evt, VariableInstanceEntity variableInstance, HistoryEventType eventType) {

    // init properties
    evt.setEventType(eventType.getEventName());
    evt.setTimestamp(ClockUtil.getCurrentTime());
    evt.setVariableInstanceId(variableInstance.getId());
    evt.setProcessInstanceId(variableInstance.getProcessInstanceId());
    evt.setExecutionId(variableInstance.getExecutionId());
    evt.setCaseInstanceId(variableInstance.getCaseInstanceId());
    evt.setCaseExecutionId(variableInstance.getCaseExecutionId());
    evt.setTaskId(variableInstance.getTaskId());
    evt.setRevision(variableInstance.getRevision());
    evt.setVariableName(variableInstance.getName());
    evt.setSerializerName(variableInstance.getSerializerName());

    ExecutionEntity execution = variableInstance.getExecution();
    if (execution != null) {
      ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
      if (definition != null) {
        evt.setProcessDefinitionId(definition.getId());
        evt.setProcessDefinitionKey(definition.getKey());
      }
    }

    CaseExecutionEntity caseExecution = variableInstance.getCaseExecution();
    if (caseExecution != null) {
      CaseDefinitionEntity definition = (CaseDefinitionEntity) caseExecution.getCaseDefinition();
      if (definition != null) {
        evt.setCaseDefinitionId(definition.getId());
        evt.setCaseDefinitionKey(definition.getKey());
      }
    }

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

  protected void initUserOperationLogEvent(UserOperationLogEntryEventEntity evt, UserOperationLogContext context,
      UserOperationLogContextEntry contextEntry, PropertyChange propertyChange) {
    // init properties
    evt.setEntityType(contextEntry.getEntityType());
    evt.setOperationType(contextEntry.getOperationType());
    evt.setOperationId(context.getOperationId());
    evt.setUserId(context.getUserId());
    evt.setProcessDefinitionId(contextEntry.getProcessDefinitionId());
    evt.setProcessDefinitionKey(contextEntry.getProcessDefinitionKey());
    evt.setProcessInstanceId(contextEntry.getProcessInstanceId());
    evt.setExecutionId(contextEntry.getExecutionId());
    evt.setCaseDefinitionId(contextEntry.getCaseDefinitionId());
    evt.setCaseInstanceId(contextEntry.getCaseInstanceId());
    evt.setCaseExecutionId(contextEntry.getCaseExecutionId());
    evt.setTaskId(contextEntry.getTaskId());
    evt.setJobId(contextEntry.getJobId());
    evt.setJobDefinitionId(contextEntry.getJobDefinitionId());
    evt.setTimestamp(ClockUtil.getCurrentTime());

    // init property value
    evt.setProperty(propertyChange.getPropertyName());
    evt.setOrgValue(propertyChange.getOrgValueString());
    evt.setNewValue(propertyChange.getNewValueString());
  }

  protected void initHistoricIncidentEvent(HistoricIncidentEventEntity evt, Incident incident, HistoryEventType eventType) {
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

    IncidentEntity incidentEntity = (IncidentEntity) incident;
    ProcessDefinitionEntity definition = incidentEntity.getProcessDefinition();
    if (definition != null) {
      evt.setProcessDefinitionKey(definition.getKey());
    }

    // init event type
    evt.setEventType(eventType.getEventName());

    // init state
    IncidentState incidentState = IncidentState.DEFAULT;
    if (HistoryEventTypes.INCIDENT_DELETE.equals(eventType)) {
      incidentState = IncidentState.DELETED;
    } else if (HistoryEventTypes.INCIDENT_RESOLVE.equals(eventType)) {
      incidentState = IncidentState.RESOLVED;
    }
    evt.setIncidentState(incidentState.getStateCode());
  }

  protected HistoryEvent createHistoricVariableEvent(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope, HistoryEventType eventType) {
    String scopeActivityInstanceId = null;
    String sourceActivityInstanceId = null;

    if(variableInstance.getExecutionId() != null) {
      ExecutionEntity scopeExecution = Context.getCommandContext()
        .getDbEntityManager()
        .selectById(ExecutionEntity.class, variableInstance.getExecutionId());

      if (variableInstance.getTaskId() == null
          && !variableInstance.isConcurrentLocal()) {
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
    // initialize sequence counter
    initSequenceCounter(variableInstance, evt);

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

  protected HistoricJobLogEventEntity newHistoricJobLogEntity(Job job) {
    return new HistoricJobLogEventEntity();
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
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_START);

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

  public HistoryEvent createProcessInstanceUpdateEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = newProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_UPDATE);

    return evt;
  }

  public HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = loadProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_END);

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
    initActivityInstanceEvent(evt, executionEntity, HistoryEventTypes.ACTIVITY_INSTANCE_START);

    // initialize sequence counter
    initSequenceCounter(executionEntity, evt);

    evt.setStartTime(ClockUtil.getCurrentTime());

    return evt;
  }

  public HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution, DelegateTask task) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricActivityInstanceEventEntity evt = loadActivityInstanceEventEntity(executionEntity);

    // initialize event
    initActivityInstanceEvent(evt, executionEntity, HistoryEventTypes.ACTIVITY_INSTANCE_UPDATE);

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

    // update sub case reference
    CaseExecutionEntity subCaseInstance = executionEntity.getSubCaseInstance();
    if (subCaseInstance != null) {
      evt.setCalledCaseInstanceId(subCaseInstance.getId());
    }

    return evt;
  }

  public HistoryEvent createActivityInstanceEndEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricActivityInstanceEventEntity evt = loadActivityInstanceEventEntity(executionEntity);
    evt.setActivityInstanceState(executionEntity.getActivityInstanceState());

    // initialize event
    initActivityInstanceEvent(evt, (ExecutionEntity) execution, HistoryEventTypes.ACTIVITY_INSTANCE_END);

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
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEventTypes.TASK_INSTANCE_CREATE);

    evt.setStartTime(ClockUtil.getCurrentTime());

    return evt;
  }

  public HistoryEvent createTaskInstanceUpdateEvt(DelegateTask task) {

    // create event instance
    HistoricTaskInstanceEventEntity evt = loadTaskInstanceEvent(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEventTypes.TASK_INSTANCE_UPDATE);

    return evt;
  }

  public HistoryEvent createTaskInstanceCompleteEvt(DelegateTask task, String deleteReason) {

    // create event instance
    HistoricTaskInstanceEventEntity evt = loadTaskInstanceEvent(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEventTypes.TASK_INSTANCE_COMPLETE);

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

    for (UserOperationLogContextEntry entry : context.getEntries()) {
      for (PropertyChange propertyChange : entry.getPropertyChanges()) {
        UserOperationLogEntryEventEntity evt = new UserOperationLogEntryEventEntity();

        initUserOperationLogEvent(evt, context, entry, propertyChange);

        historyEvents.add(evt);
      }
    }

    return historyEvents;
  }

  // variables /////////////////////////////////

  public HistoryEvent createHistoricVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEventTypes.VARIABLE_INSTANCE_CREATE);
  }

  public HistoryEvent createHistoricVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEventTypes.VARIABLE_INSTANCE_DELETE);
  }

  public HistoryEvent createHistoricVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope) {
    return createHistoricVariableEvent(variableInstance, sourceVariableScope, HistoryEventTypes.VARIABLE_INSTANCE_UPDATE);
  }

  // form Properties ///////////////////////////

  public HistoryEvent createFormPropertyUpdateEvt(ExecutionEntity execution, String propertyId, String propertyValue, String taskId) {

    final IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();

    HistoricFormPropertyEventEntity historicFormPropertyEntity = newHistoricFormPropertyEvent();

    historicFormPropertyEntity.setId(idGenerator.getNextId());
    historicFormPropertyEntity.setEventType(HistoryEventTypes.FORM_PROPERTY_UPDATE.getEventName());
    historicFormPropertyEntity.setTimestamp(ClockUtil.getCurrentTime());
    historicFormPropertyEntity.setActivityInstanceId(execution.getActivityInstanceId());
    historicFormPropertyEntity.setExecutionId(execution.getId());
    historicFormPropertyEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    historicFormPropertyEntity.setProcessInstanceId(execution.getProcessInstanceId());
    historicFormPropertyEntity.setPropertyId(propertyId);
    historicFormPropertyEntity.setPropertyValue(propertyValue);
    historicFormPropertyEntity.setTaskId(taskId);

    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    if (definition != null) {
      historicFormPropertyEntity.setProcessDefinitionKey(definition.getKey());
    }

    // initialize sequence counter
    initSequenceCounter(execution, historicFormPropertyEntity);

    return historicFormPropertyEntity;
  }

  // Incidents //////////////////////////////////

  public HistoryEvent createHistoricIncidentCreateEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_CREATE);
  }

  public HistoryEvent createHistoricIncidentResolveEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_RESOLVE);
  }

  public HistoryEvent createHistoricIncidentDeleteEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_DELETE);
  }

  protected HistoryEvent createHistoricIncidentEvt(Incident incident, HistoryEventTypes eventType) {
    // create event
    HistoricIncidentEventEntity evt = loadIncidentEvent(incident);
    // initialize
    initHistoricIncidentEvent(evt, incident, eventType);

    if (!HistoryEventTypes.INCIDENT_CREATE.equals(eventType)) {
      evt.setEndTime(ClockUtil.getCurrentTime());
    }

    return evt;
  }

  // Job Log

  public HistoryEvent createHistoricJobLogCreateEvt(Job job) {
    return createHistoricJobLogEvt(job, HistoryEventTypes.JOB_CREATE);
  }

  public HistoryEvent createHistoricJobLogFailedEvt(Job job, Throwable exception) {
    HistoricJobLogEventEntity event = (HistoricJobLogEventEntity) createHistoricJobLogEvt(job, HistoryEventTypes.JOB_FAIL);

    if(exception != null) {
      // exception message
      event.setJobExceptionMessage(exception.getMessage());

      // stacktrace
      String exceptionStacktrace = getJobExceptionStacktrace(exception);
      byte[] exceptionBytes = toByteArray(exceptionStacktrace);
      ByteArrayEntity byteArray = createJobExceptionByteArray(exceptionBytes);
      event.setExceptionByteArrayId(byteArray.getId());
    }

    return event;
  }

  public HistoryEvent createHistoricJobLogSuccessfulEvt(Job job) {
    return createHistoricJobLogEvt(job, HistoryEventTypes.JOB_SUCCESS);
  }

  public HistoryEvent createHistoricJobLogDeleteEvt(Job job) {
    return createHistoricJobLogEvt(job, HistoryEventTypes.JOB_DELETE);
  }

  protected HistoryEvent createHistoricJobLogEvt(Job job, HistoryEventType eventType) {
    HistoricJobLogEventEntity event = newHistoricJobLogEntity(job);
    initHistoricJobLogEvent(event, job, eventType);
    return event;
  }

  protected void initHistoricJobLogEvent(HistoricJobLogEventEntity evt, Job job, HistoryEventType eventType) {
    evt.setTimestamp(new Date());

    JobEntity jobEntity = (JobEntity) job;
    evt.setJobId(jobEntity.getId());
    evt.setJobDueDate(jobEntity.getDuedate());
    evt.setJobRetries(jobEntity.getRetries());
    evt.setJobPriority(jobEntity.getPriority());

    JobDefinition jobDefinition = jobEntity.getJobDefinition();
    if (jobDefinition != null) {
      evt.setJobDefinitionId(jobDefinition.getId());
      evt.setJobDefinitionType(jobDefinition.getJobType());
      evt.setJobDefinitionConfiguration(jobDefinition.getJobConfiguration());
    }
    else {
      // in case of async signal there does not exist a job definition
      // but we use the jobHandlerType as jobDefinitionType
      evt.setJobDefinitionType(jobEntity.getJobHandlerType());
    }

    evt.setActivityId(jobEntity.getActivityId());
    evt.setExecutionId(jobEntity.getExecutionId());
    evt.setProcessInstanceId(jobEntity.getProcessInstanceId());
    evt.setProcessDefinitionId(jobEntity.getProcessDefinitionId());
    evt.setProcessDefinitionKey(jobEntity.getProcessDefinitionKey());
    evt.setDeploymentId(jobEntity.getDeploymentId());

    // initialize sequence counter
    initSequenceCounter(jobEntity, evt);

    JobState state = null;
    if (HistoryEventTypes.JOB_CREATE.equals(eventType)) {
      state = JobState.CREATED;
    }
    else if (HistoryEventTypes.JOB_FAIL.equals(eventType)) {
      state = JobState.FAILED;
    }
    else if (HistoryEventTypes.JOB_SUCCESS.equals(eventType)) {
      state = JobState.SUCCESSFUL;
    }
    else if (HistoryEventTypes.JOB_DELETE.equals(eventType)) {
      state = JobState.DELETED;
    }
    evt.setState(state.getStateCode());
  }

  // sequence counter //////////////////////////////////////////////////////

  protected void initSequenceCounter(ExecutionEntity execution, HistoryEvent event) {
    initSequenceCounter(execution.getSequenceCounter(), event);
  }

  protected void initSequenceCounter(VariableInstanceEntity variable, HistoryEvent event) {
    initSequenceCounter(variable.getSequenceCounter(), event);
  }

  protected void initSequenceCounter(JobEntity job, HistoryEvent event) {
    initSequenceCounter(job.getSequenceCounter(), event);
  }

  protected void initSequenceCounter(long sequenceCounter, HistoryEvent event) {
    event.setSequenceCounter(sequenceCounter);
  }

}
