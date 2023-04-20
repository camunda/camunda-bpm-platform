/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.producer;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.createJobExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.getExceptionStacktrace;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.ExternalTaskState;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.IncidentState;
import org.camunda.bpm.engine.history.JobState;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.ConfigurationLogger;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.DefaultHistoryRemovalTimeProvider;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntry;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
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
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.IdentityLink;

/**
 * @author Daniel Meyer
 * @author Ingo Richtsmeier
 *
 */
public class DefaultHistoryEventProducer implements HistoryEventProducer {

  protected final static ConfigurationLogger LOG = ProcessEngineLogger.CONFIG_LOGGER;

  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt, ExecutionEntity execution, HistoryEventType eventType) {
    PvmScope eventSource = execution.getActivity();
    if (eventSource == null) {
      eventSource = (PvmScope) execution.getEventSource();
    }
    String activityInstanceId = execution.getActivityInstanceId();

    String parentActivityInstanceId = null;
    ExecutionEntity parentExecution = execution.getParent();

    if (parentExecution != null && CompensationBehavior.isCompensationThrowing(parentExecution) && execution.getActivity() != null) {
      parentActivityInstanceId = CompensationBehavior.getParentActivityInstanceId(execution);
    } else {
      parentActivityInstanceId = execution.getParentActivityInstanceId();
    }

    initActivityInstanceEvent(evt,
        execution,
        eventSource,
        activityInstanceId,
        parentActivityInstanceId,
        eventType);
  }

  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt, MigratingActivityInstance migratingActivityInstance, HistoryEventType eventType) {
    PvmScope eventSource = migratingActivityInstance.getTargetScope();
    String activityInstanceId = migratingActivityInstance.getActivityInstanceId();

    MigratingActivityInstance parentInstance = migratingActivityInstance.getParent();
    String parentActivityInstanceId = null;
    if (parentInstance != null) {
      parentActivityInstanceId = parentInstance.getActivityInstanceId();
    }

    ExecutionEntity execution = migratingActivityInstance.resolveRepresentativeExecution();

    initActivityInstanceEvent(evt,
        execution,
        eventSource,
        activityInstanceId,
        parentActivityInstanceId,
        eventType);
  }

  protected void initActivityInstanceEvent(HistoricActivityInstanceEventEntity evt,
      ExecutionEntity execution,
      PvmScope eventSource,
      String activityInstanceId,
      String parentActivityInstanceId,
      HistoryEventType eventType) {

    evt.setId(activityInstanceId);
    evt.setEventType(eventType.getEventName());
    evt.setActivityInstanceId(activityInstanceId);
    evt.setParentActivityInstanceId(parentActivityInstanceId);
    evt.setProcessDefinitionId(execution.getProcessDefinitionId());
    evt.setProcessInstanceId(execution.getProcessInstanceId());
    evt.setExecutionId(execution.getId());
    evt.setTenantId(execution.getTenantId());
    evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(evt);
    }

    ProcessDefinitionEntity definition = execution.getProcessDefinition();
    if (definition != null) {
      evt.setProcessDefinitionKey(definition.getKey());
    }

    evt.setActivityId(eventSource.getId());
    evt.setActivityName((String) eventSource.getProperty("name"));
    evt.setActivityType((String) eventSource.getProperty("type"));

    // update sub process reference
    ExecutionEntity subProcessInstance = execution.getSubProcessInstance();
    if (subProcessInstance != null) {
      evt.setCalledProcessInstanceId(subProcessInstance.getId());
    }

    // update sub case reference
    CaseExecutionEntity subCaseInstance = execution.getSubCaseInstance();
    if (subCaseInstance != null) {
      evt.setCalledCaseInstanceId(subCaseInstance.getId());
    }
  }


  protected void initProcessInstanceEvent(HistoricProcessInstanceEventEntity evt, ExecutionEntity execution, HistoryEventType eventType) {

    String processDefinitionId = execution.getProcessDefinitionId();
    String processInstanceId = execution.getProcessInstanceId();
    String executionId = execution.getId();
    // the given execution is the process instance!
    String caseInstanceId = execution.getCaseInstanceId();
    String tenantId = execution.getTenantId();

    ProcessDefinitionEntity definition = execution.getProcessDefinition();
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
    evt.setTenantId(tenantId);
    evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

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
    String tenantId = taskEntity.getTenantId();

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
    evt.setTenantId(tenantId);

    ExecutionEntity execution = taskEntity.getExecution();
    if (execution != null) {
      evt.setActivityInstanceId(execution.getActivityInstanceId());
      evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        provideRemovalTime(evt);
      }
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
    evt.setTenantId(variableInstance.getTenantId());
    evt.setUserOperationId(Context.getCommandContext().getOperationId());

    ExecutionEntity execution = variableInstance.getExecution();
    if (execution != null) {
      ProcessDefinitionEntity definition = execution.getProcessDefinition();
      if (definition != null) {
        evt.setProcessDefinitionId(definition.getId());
        evt.setProcessDefinitionKey(definition.getKey());
      }
      evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        provideRemovalTime(evt);
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
      evt.setByteValue(variableInstance.getByteArrayValue());
    }
  }

  protected void initUserOperationLogEvent(UserOperationLogEntryEventEntity evt, UserOperationLogContext context,
      UserOperationLogContextEntry contextEntry, PropertyChange propertyChange) {
    // init properties
    evt.setDeploymentId(contextEntry.getDeploymentId());
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
    evt.setBatchId(contextEntry.getBatchId());
    evt.setCategory(contextEntry.getCategory());
    evt.setTimestamp(ClockUtil.getCurrentTime());
    evt.setRootProcessInstanceId(contextEntry.getRootProcessInstanceId());
    evt.setExternalTaskId(contextEntry.getExternalTaskId());
    evt.setAnnotation(contextEntry.getAnnotation());
    evt.setTenantId(contextEntry.getTenantId());

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(evt);
    }

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
    evt.setTenantId(incident.getTenantId());
    evt.setJobDefinitionId(incident.getJobDefinitionId());
    evt.setHistoryConfiguration(incident.getHistoryConfiguration());
    evt.setFailedActivityId(incident.getFailedActivityId());
    evt.setAnnotation(incident.getAnnotation());

    String jobId = incident.getConfiguration();
    if (jobId != null && isHistoryRemovalTimeStrategyStart()) {
      HistoricBatchEntity historicBatch = getHistoricBatchByJobId(jobId);
      if (historicBatch != null) {
        evt.setRemovalTime(historicBatch.getRemovalTime());
      }
    }

    IncidentEntity incidentEntity = (IncidentEntity) incident;
    ProcessDefinitionEntity definition = incidentEntity.getProcessDefinition();
    if (definition != null) {
      evt.setProcessDefinitionKey(definition.getKey());
    }

    ExecutionEntity execution = incidentEntity.getExecution();
    if (execution != null) {
      evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        provideRemovalTime(evt);
      }
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
    else if (variableInstance.getCaseExecutionId() != null) {
      scopeActivityInstanceId = variableInstance.getCaseExecutionId();
    }

    ExecutionEntity sourceExecution = null;
    CaseExecutionEntity sourceCaseExecution = null;
    if (sourceVariableScope instanceof ExecutionEntity) {
      sourceExecution = (ExecutionEntity) sourceVariableScope;
      sourceActivityInstanceId = sourceExecution.getActivityInstanceId();

    } else if (sourceVariableScope instanceof TaskEntity) {
      sourceExecution = ((TaskEntity) sourceVariableScope).getExecution();
      if (sourceExecution != null) {
        sourceActivityInstanceId = sourceExecution.getActivityInstanceId();
      }
      else {
        sourceCaseExecution = ((TaskEntity) sourceVariableScope).getCaseExecution();
        if (sourceCaseExecution != null) {
          sourceActivityInstanceId = sourceCaseExecution.getId();
        }
      }
    }
    else if (sourceVariableScope instanceof CaseExecutionEntity) {
      sourceCaseExecution = (CaseExecutionEntity) sourceVariableScope;
      sourceActivityInstanceId = sourceCaseExecution.getId();
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

    // mark initial variables on process start
    if (sourceExecution != null && sourceExecution.isProcessInstanceStarting()
        && HistoryEventTypes.VARIABLE_INSTANCE_CREATE.equals(eventType)) {

      if (variableInstance.getSequenceCounter() == 1) {
        evt.setInitial(true);
      }

      if (sourceActivityInstanceId == null && sourceExecution.getActivity() != null && sourceExecution.getTransition() == null) {
        evt.setActivityInstanceId(sourceExecution.getProcessInstanceId());
      }
    }

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

  protected HistoricBatchEntity newBatchEventEntity(BatchEntity batch) {
    return new HistoricBatchEntity();
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

  protected HistoricBatchEntity loadBatchEntity(BatchEntity batch) {
    return newBatchEventEntity(batch);
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

    //state
    evt.setState(HistoricProcessInstance.STATE_ACTIVE);

    // set start user Id
    evt.setStartUserId(Context.getCommandContext().getAuthenticatedUserId());

    if (isHistoryRemovalTimeStrategyStart()) {
      if (isRootProcessInstance(evt)) {
        Date removalTime = calculateRemovalTime(evt);
        evt.setRemovalTime(removalTime);
      } else {
        provideRemovalTime(evt);
      }
    }

    return evt;
  }

  public HistoryEvent createProcessInstanceUpdateEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = loadProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_UPDATE);

    if (executionEntity.isSuspended()) {
      evt.setState(HistoricProcessInstance.STATE_SUSPENDED);
    } else {
      evt.setState(HistoricProcessInstance.STATE_ACTIVE);
    }

    return evt;
  }

  @Override
  public HistoryEvent createProcessInstanceMigrateEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = newProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_MIGRATE);

    if (executionEntity.isSuspended()) {
      evt.setState(HistoricProcessInstance.STATE_SUSPENDED);
    } else {
      evt.setState(HistoricProcessInstance.STATE_ACTIVE);
    }

    return evt;
  }

  public HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricProcessInstanceEventEntity evt = loadProcessInstanceEventEntity(executionEntity);

    // initialize event
    initProcessInstanceEvent(evt, executionEntity, HistoryEventTypes.PROCESS_INSTANCE_END);

    determineEndState(executionEntity, evt);

    // set end activity id
    evt.setEndActivityId(executionEntity.getActivityId());
    evt.setEndTime(ClockUtil.getCurrentTime());

    if(evt.getStartTime() != null) {
      evt.setDurationInMillis(evt.getEndTime().getTime()-evt.getStartTime().getTime());
    }

    if (isRootProcessInstance(evt) && isHistoryRemovalTimeStrategyEnd()) {
      Date removalTime = calculateRemovalTime(evt);

      if (removalTime != null) {
        addRemovalTimeToHistoricProcessInstances(evt.getRootProcessInstanceId(), removalTime);

        if (isDmnEnabled()) {
          addRemovalTimeToHistoricDecisions(evt.getRootProcessInstanceId(), removalTime);
        }
      }
    }

    // set delete reason (if applicable).
    if (executionEntity.getDeleteReason() != null) {
      evt.setDeleteReason(executionEntity.getDeleteReason());
    }

    return evt;
  }

  protected void addRemovalTimeToHistoricDecisions(String rootProcessInstanceId, Date removalTime) {
    Context.getCommandContext()
      .getHistoricDecisionInstanceManager()
      .addRemovalTimeToDecisionsByRootProcessInstanceId(rootProcessInstanceId, removalTime);
  }

  protected void addRemovalTimeToHistoricProcessInstances(String rootProcessInstanceId, Date removalTime) {
    Context.getCommandContext()
      .getHistoricProcessInstanceManager()
      .addRemovalTimeToProcessInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime);
  }

  protected boolean isDmnEnabled() {
    return Context.getCommandContext()
      .getProcessEngineConfiguration()
      .isDmnEnabled();
  }

  protected void determineEndState(ExecutionEntity executionEntity, HistoricProcessInstanceEventEntity evt) {
    //determine state
    if (executionEntity.getActivity() != null) {
      evt.setState(HistoricProcessInstance.STATE_COMPLETED);
    } else {
      if (executionEntity.isExternallyTerminated()) {
        evt.setState(HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED);
      } else if (!executionEntity.isExternallyTerminated()) {
        evt.setState(HistoricProcessInstance.STATE_INTERNALLY_TERMINATED);
      }
    }
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

  @Override
  public HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution) {
    return createActivityInstanceUpdateEvt(execution, null);
  }

  @Override
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

    return evt;
  }

  @Override
  public HistoryEvent createActivityInstanceMigrateEvt(MigratingActivityInstance actInstance) {

    // create event instance
    HistoricActivityInstanceEventEntity evt = loadActivityInstanceEventEntity(actInstance.resolveRepresentativeExecution());

    // initialize event
    initActivityInstanceEvent(evt, actInstance, HistoryEventTypes.ACTIVITY_INSTANCE_MIGRATE);

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

  @Override
  public HistoryEvent createTaskInstanceMigrateEvt(DelegateTask task) {
    // create event instance
    HistoricTaskInstanceEventEntity evt = loadTaskInstanceEvent(task);

    // initialize event
    initTaskInstanceEvent(evt, (TaskEntity) task, HistoryEventTypes.TASK_INSTANCE_MIGRATE);

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

    String operationId = Context.getCommandContext().getOperationId();
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

  @Override
  public HistoryEvent createHistoricVariableMigrateEvt(VariableInstanceEntity variableInstance) {
    return createHistoricVariableEvent(variableInstance, null, HistoryEventTypes.VARIABLE_INSTANCE_MIGRATE);
  }

  // form Properties ///////////////////////////

  public HistoryEvent createFormPropertyUpdateEvt(ExecutionEntity execution, String propertyId, String propertyValue, String taskId) {

    final IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();

    HistoricFormPropertyEventEntity historicFormPropertyEntity = newHistoricFormPropertyEvent();

    historicFormPropertyEntity.setId(idGenerator.getNextId());
    historicFormPropertyEntity.setEventType(HistoryEventTypes.FORM_PROPERTY_UPDATE.getEventName());
    historicFormPropertyEntity.setTimestamp(ClockUtil.getCurrentTime());
    historicFormPropertyEntity.setExecutionId(execution.getId());
    historicFormPropertyEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    historicFormPropertyEntity.setProcessInstanceId(execution.getProcessInstanceId());
    historicFormPropertyEntity.setPropertyId(propertyId);
    historicFormPropertyEntity.setPropertyValue(propertyValue);
    historicFormPropertyEntity.setTaskId(taskId);
    historicFormPropertyEntity.setTenantId(execution.getTenantId());
    historicFormPropertyEntity.setUserOperationId(Context.getCommandContext().getOperationId());
    historicFormPropertyEntity.setRootProcessInstanceId(execution.getRootProcessInstanceId());

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(historicFormPropertyEntity);
    }

    ProcessDefinitionEntity definition = execution.getProcessDefinition();
    if (definition != null) {
      historicFormPropertyEntity.setProcessDefinitionKey(definition.getKey());
    }

    // initialize sequence counter
    initSequenceCounter(execution, historicFormPropertyEntity);

    if (execution.isProcessInstanceStarting()) {
      // instantiate activity instance id as process instance id when starting a process instance
      // via a form
      historicFormPropertyEntity.setActivityInstanceId(execution.getProcessInstanceId());
    } else {
      historicFormPropertyEntity.setActivityInstanceId(execution.getActivityInstanceId());
    }

    return historicFormPropertyEntity;
  }

  // Incidents //////////////////////////////////

  public HistoryEvent createHistoricIncidentCreateEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_CREATE);
  }

  public HistoryEvent createHistoricIncidentUpdateEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_UPDATE);
  }

  public HistoryEvent createHistoricIncidentResolveEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_RESOLVE);
  }

  public HistoryEvent createHistoricIncidentDeleteEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_DELETE);
  }

  public HistoryEvent createHistoricIncidentMigrateEvt(Incident incident) {
    return createHistoricIncidentEvt(incident, HistoryEventTypes.INCIDENT_MIGRATE);
  }

  protected HistoryEvent createHistoricIncidentEvt(Incident incident, HistoryEventTypes eventType) {
    // create event
    HistoricIncidentEventEntity evt = loadIncidentEvent(incident);
    // initialize
    initHistoricIncidentEvent(evt, incident, eventType);

    if (HistoryEventTypes.INCIDENT_RESOLVE.equals(eventType) || HistoryEventTypes.INCIDENT_DELETE.equals(eventType)) {
      evt.setEndTime(ClockUtil.getCurrentTime());
    }

    return evt;
  }

  // Historic identity link
  @Override
  public HistoryEvent createHistoricIdentityLinkAddEvent(IdentityLink identityLink) {
    return createHistoricIdentityLinkEvt(identityLink, HistoryEventTypes.IDENTITY_LINK_ADD);
  }

  @Override
  public HistoryEvent createHistoricIdentityLinkDeleteEvent(IdentityLink identityLink) {
    return createHistoricIdentityLinkEvt(identityLink, HistoryEventTypes.IDENTITY_LINK_DELETE);
  }

  protected HistoryEvent createHistoricIdentityLinkEvt(IdentityLink identityLink, HistoryEventTypes eventType) {
    // create historic identity link event
    HistoricIdentityLinkLogEventEntity evt = newIdentityLinkEventEntity();
    // Mapping all the values of identity link to HistoricIdentityLinkEvent
    initHistoricIdentityLinkEvent(evt, identityLink, eventType);
    return evt;
  }

  protected HistoricIdentityLinkLogEventEntity newIdentityLinkEventEntity() {
    return new HistoricIdentityLinkLogEventEntity();
  }

  protected void initHistoricIdentityLinkEvent(HistoricIdentityLinkLogEventEntity evt, IdentityLink identityLink, HistoryEventType eventType) {

    if (identityLink.getTaskId() != null) {
      TaskEntity task = Context
          .getCommandContext()
          .getTaskManager()
          .findTaskById(identityLink.getTaskId());

      evt.setProcessDefinitionId(task.getProcessDefinitionId());

      if (task.getProcessDefinition() != null) {
        evt.setProcessDefinitionKey(task.getProcessDefinition().getKey());
      }

      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

        if (isHistoryRemovalTimeStrategyStart()) {
          provideRemovalTime(evt);
        }
      }
    }

    if (identityLink.getProcessDefId() != null) {
      evt.setProcessDefinitionId(identityLink.getProcessDefId());

      ProcessDefinitionEntity definition = Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .findProcessDefinitionFromCache(identityLink.getProcessDefId());
      evt.setProcessDefinitionKey(definition.getKey());
    }

    evt.setTime(ClockUtil.getCurrentTime());
    evt.setType(identityLink.getType());
    evt.setUserId(identityLink.getUserId());
    evt.setGroupId(identityLink.getGroupId());
    evt.setTaskId(identityLink.getTaskId());
    evt.setTenantId(identityLink.getTenantId());
    // There is a conflict in HistoryEventTypes for 'delete' keyword,
    // So HistoryEventTypes.IDENTITY_LINK_ADD /
    // HistoryEventTypes.IDENTITY_LINK_DELETE is provided with the event name
    // 'add-identity-link' /'delete-identity-link'
    // and changed to 'add'/'delete' (While inserting it into the database) on
    // Historic identity link add / delete event
    String operationType = "add";
    if (eventType.getEventName().equals(HistoryEventTypes.IDENTITY_LINK_DELETE.getEventName())) {
      operationType = "delete";
    }

    evt.setOperationType(operationType);
    evt.setEventType(eventType.getEventName());
    evt.setAssignerId(Context.getCommandContext().getAuthenticatedUserId());
  }
  // Batch

  @Override
  public HistoryEvent createBatchStartEvent(Batch batch) {
    HistoryEvent historicBatch = createBatchEvent((BatchEntity) batch, HistoryEventTypes.BATCH_START);

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime((HistoricBatchEntity) historicBatch);
    }

    return historicBatch;
  }

  @Override
  public HistoryEvent createBatchEndEvent(Batch batch) {
    HistoryEvent historicBatch = createBatchEvent((BatchEntity) batch, HistoryEventTypes.BATCH_END);

    if (isHistoryRemovalTimeStrategyEnd()) {
      provideRemovalTime((HistoricBatchEntity) historicBatch);

      addRemovalTimeToHistoricJobLog((HistoricBatchEntity) historicBatch);
      addRemovalTimeToHistoricIncidents((HistoricBatchEntity) historicBatch);
    }

    return historicBatch;
  }

  @Override
  public HistoryEvent createBatchUpdateEvent(Batch batch) {
    return createBatchEvent((BatchEntity) batch, HistoryEventTypes.BATCH_UPDATE);
  }

  protected HistoryEvent createBatchEvent(BatchEntity batch, HistoryEventTypes eventType) {
    HistoricBatchEntity event = loadBatchEntity(batch);

    event.setId(batch.getId());
    event.setType(batch.getType());
    event.setTotalJobs(batch.getTotalJobs());
    event.setBatchJobsPerSeed(batch.getBatchJobsPerSeed());
    event.setInvocationsPerBatchJob(batch.getInvocationsPerBatchJob());
    event.setSeedJobDefinitionId(batch.getSeedJobDefinitionId());
    event.setMonitorJobDefinitionId(batch.getMonitorJobDefinitionId());
    event.setBatchJobDefinitionId(batch.getBatchJobDefinitionId());
    event.setTenantId(batch.getTenantId());
    event.setEventType(eventType.getEventName());

    if (HistoryEventTypes.BATCH_START.equals(eventType)) {
      event.setStartTime(batch.getStartTime());
      event.setCreateUserId(Context.getCommandContext().getAuthenticatedUserId());
    }

    if (HistoryEventTypes.BATCH_END.equals(eventType)) {
      event.setEndTime(ClockUtil.getCurrentTime());
    }

    if (HistoryEventTypes.BATCH_UPDATE.equals(eventType)) {
      event.setExecutionStartTime(batch.getExecutionStartTime());
    }

    return event;
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
      String exceptionStacktrace = getExceptionStacktrace(exception);
      byte[] exceptionBytes = toByteArray(exceptionStacktrace);

      ByteArrayEntity byteArray = createJobExceptionByteArray(exceptionBytes, ResourceTypes.HISTORY);
      byteArray.setRootProcessInstanceId(event.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        byteArray.setRemovalTime(event.getRemovalTime());
      }

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
    Date currentTime = ClockUtil.getCurrentTime();
    evt.setTimestamp(currentTime);

    JobEntity jobEntity = (JobEntity) job;
    evt.setJobId(jobEntity.getId());
    evt.setJobDueDate(jobEntity.getDuedate());
    evt.setJobRetries(jobEntity.getRetries());
    evt.setJobPriority(jobEntity.getPriority());

    String hostName = Context.getCommandContext().getProcessEngineConfiguration().getHostname();
    evt.setHostname(hostName);

    if (HistoryCleanupJobHandler.TYPE.equals(jobEntity.getJobHandlerType())) {
      String timeToLive = Context.getProcessEngineConfiguration().getHistoryCleanupJobLogTimeToLive();
      if(timeToLive != null) {
        try {
          Integer timeToLiveDays = ParseUtil.parseHistoryTimeToLive(timeToLive);
          Date removalTime = DefaultHistoryRemovalTimeProvider.determineRemovalTime(currentTime, timeToLiveDays);
          evt.setRemovalTime(removalTime);
        } catch (ProcessEngineException e) {
          ProcessEngineException wrappedException = LOG.invalidPropertyValue("historyCleanupJobLogTimeToLive", timeToLive, e);
          LOG.invalidPropertyValue(wrappedException);
        }
      }
    }

    JobDefinition jobDefinition = jobEntity.getJobDefinition();
    if (jobDefinition != null) {
      evt.setJobDefinitionId(jobDefinition.getId());
      evt.setJobDefinitionType(jobDefinition.getJobType());
      evt.setJobDefinitionConfiguration(jobDefinition.getJobConfiguration());

      String historicBatchId = jobDefinition.getJobConfiguration();
      if (historicBatchId != null && isHistoryRemovalTimeStrategyStart()) {
        HistoricBatchEntity historicBatch = getHistoricBatchById(historicBatchId);
        if (historicBatch != null) {
          evt.setRemovalTime(historicBatch.getRemovalTime());
        }
      }
    }
    else {
      // in case of async signal there does not exist a job definition
      // but we use the jobHandlerType as jobDefinitionType
      evt.setJobDefinitionType(jobEntity.getJobHandlerType());
    }

    evt.setActivityId(jobEntity.getActivityId());
    evt.setFailedActivityId(jobEntity.getFailedActivityId());
    evt.setExecutionId(jobEntity.getExecutionId());
    evt.setProcessInstanceId(jobEntity.getProcessInstanceId());
    evt.setProcessDefinitionId(jobEntity.getProcessDefinitionId());
    evt.setProcessDefinitionKey(jobEntity.getProcessDefinitionKey());
    evt.setDeploymentId(jobEntity.getDeploymentId());
    evt.setTenantId(jobEntity.getTenantId());

    ExecutionEntity execution = jobEntity.getExecution();
    if (execution != null) {
      evt.setRootProcessInstanceId(execution.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        provideRemovalTime(evt);
      }
    }

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

  @Override
  public HistoryEvent createHistoricExternalTaskLogCreatedEvt(ExternalTask task) {
    return initHistoricExternalTaskLog((ExternalTaskEntity) task, ExternalTaskState.CREATED);
  }

  @Override
  public HistoryEvent createHistoricExternalTaskLogFailedEvt(ExternalTask task) {
    HistoricExternalTaskLogEntity event = initHistoricExternalTaskLog((ExternalTaskEntity) task, ExternalTaskState.FAILED);
    event.setErrorMessage(task.getErrorMessage());
    String errorDetails = ((ExternalTaskEntity) task).getErrorDetails();
    if( errorDetails != null) {
      event.setErrorDetails(errorDetails);
    }
    return event;
  }

  @Override
  public HistoryEvent createHistoricExternalTaskLogSuccessfulEvt(ExternalTask task) {
    return initHistoricExternalTaskLog((ExternalTaskEntity) task, ExternalTaskState.SUCCESSFUL);
  }

  @Override
  public HistoryEvent createHistoricExternalTaskLogDeletedEvt(ExternalTask task) {
    return initHistoricExternalTaskLog((ExternalTaskEntity) task, ExternalTaskState.DELETED);
  }

  protected HistoricExternalTaskLogEntity initHistoricExternalTaskLog(ExternalTaskEntity entity, ExternalTaskState state) {
    HistoricExternalTaskLogEntity event = new HistoricExternalTaskLogEntity();
    event.setTimestamp(ClockUtil.getCurrentTime());
    event.setExternalTaskId(entity.getId());
    event.setTopicName(entity.getTopicName());
    event.setWorkerId(entity.getWorkerId());

    event.setPriority(entity.getPriority());
    event.setRetries(entity.getRetries());

    event.setActivityId(entity.getActivityId());
    event.setActivityInstanceId(entity.getActivityInstanceId());
    event.setExecutionId(entity.getExecutionId());

    event.setProcessInstanceId(entity.getProcessInstanceId());
    event.setProcessDefinitionId(entity.getProcessDefinitionId());
    event.setProcessDefinitionKey(entity.getProcessDefinitionKey());
    event.setTenantId(entity.getTenantId());
    event.setState(state.getStateCode());

    ExecutionEntity execution = entity.getExecution();
    if (execution != null) {
      event.setRootProcessInstanceId(execution.getRootProcessInstanceId());

      if (isHistoryRemovalTimeStrategyStart()) {
        provideRemovalTime(event);
      }
    }

    return event;
  }

  protected boolean isRootProcessInstance(HistoricProcessInstanceEventEntity evt) {
    return evt.getProcessInstanceId().equals(evt.getRootProcessInstanceId());
  }

  protected boolean isHistoryRemovalTimeStrategyStart() {
    return HISTORY_REMOVAL_TIME_STRATEGY_START.equals(getHistoryRemovalTimeStrategy());
  }

  protected boolean isHistoryRemovalTimeStrategyEnd() {
    return HISTORY_REMOVAL_TIME_STRATEGY_END.equals(getHistoryRemovalTimeStrategy());
  }

  protected String getHistoryRemovalTimeStrategy() {
    return Context.getProcessEngineConfiguration()
      .getHistoryRemovalTimeStrategy();
  }

  protected Date calculateRemovalTime(HistoryEvent historyEvent) {
    String processDefinitionId = historyEvent.getProcessDefinitionId();
    ProcessDefinition processDefinition = findProcessDefinitionById(processDefinitionId);

    return Context.getProcessEngineConfiguration()
      .getHistoryRemovalTimeProvider()
      .calculateRemovalTime((HistoricProcessInstanceEventEntity) historyEvent, processDefinition);
  }

  protected Date calculateRemovalTime(HistoricBatchEntity historicBatch) {
    return Context.getProcessEngineConfiguration()
      .getHistoryRemovalTimeProvider()
      .calculateRemovalTime(historicBatch);
  }

  protected void provideRemovalTime(HistoricBatchEntity historicBatch) {
    Date removalTime = calculateRemovalTime(historicBatch);
    if (removalTime != null) {
      historicBatch.setRemovalTime(removalTime);
    }
  }

  protected void provideRemovalTime(HistoryEvent historyEvent) {
    String rootProcessInstanceId = historyEvent.getRootProcessInstanceId();
    if (rootProcessInstanceId != null) {
      HistoricProcessInstanceEventEntity historicRootProcessInstance =
        getHistoricRootProcessInstance(rootProcessInstanceId);

      if (historicRootProcessInstance != null) {
        Date removalTime = historicRootProcessInstance.getRemovalTime();
        historyEvent.setRemovalTime(removalTime);
      }
    }
  }

  protected HistoricProcessInstanceEventEntity getHistoricRootProcessInstance(String rootProcessInstanceId) {
    return Context.getCommandContext()
      .getDbEntityManager()
      .selectById(HistoricProcessInstanceEventEntity.class, rootProcessInstanceId);
  }

  protected ProcessDefinition findProcessDefinitionById(String processDefinitionId) {
    return Context.getCommandContext()
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId);
  }

  protected HistoricBatchEntity getHistoricBatchById(String batchId) {
    return Context.getCommandContext()
      .getHistoricBatchManager()
      .findHistoricBatchById(batchId);
  }

  protected HistoricBatchEntity getHistoricBatchByJobId(String jobId) {
    return Context.getCommandContext()
      .getHistoricBatchManager()
      .findHistoricBatchByJobId(jobId);
  }

  protected void addRemovalTimeToHistoricJobLog(HistoricBatchEntity historicBatch) {
    Date removalTime = historicBatch.getRemovalTime();
    if (removalTime != null) {
      Context.getCommandContext()
        .getHistoricJobLogManager()
        .addRemovalTimeToJobLogByBatchId(historicBatch.getId(), removalTime);
    }
  }

  protected void addRemovalTimeToHistoricIncidents(HistoricBatchEntity historicBatch) {
    Date removalTime = historicBatch.getRemovalTime();
    if (removalTime != null) {
      Context.getCommandContext()
        .getHistoricIncidentManager()
        .addRemovalTimeToHistoricIncidentsByBatchId(historicBatch.getId(), removalTime);
    }
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
