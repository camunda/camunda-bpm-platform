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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_IDENTITY_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_TASK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogContext;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserOperationLogQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.runtime.Job;

/**
 * Manager for {@link UserOperationLogEntryEventEntity} that also provides a generic and some specific log methods.
 *
 * @author Danny Gräf
 */
public class UserOperationLogManager extends AbstractHistoricManager {

  public UserOperationLogEntry findOperationLogById(String entryId) {
    return getDbEntityManager().selectById(UserOperationLogEntryEventEntity.class, entryId);
  }

  public long findOperationLogEntryCountByQueryCriteria(UserOperationLogQueryImpl query) {
    getAuthorizationManager().configureUserOperationLogQuery(query);
    return (Long) getDbEntityManager().selectOne("selectUserOperationLogEntryCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<UserOperationLogEntry> findOperationLogEntriesByQueryCriteria(UserOperationLogQueryImpl query, Page page) {
    getAuthorizationManager().configureUserOperationLogQuery(query);
    return getDbEntityManager().selectList("selectUserOperationLogEntriesByQueryCriteria", query, page);
  }

  public void deleteOperationLogEntriesByProcessInstanceId(String historicProcessInstanceId) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByProcessInstanceId", historicProcessInstanceId);
  }

  public void deleteOperationLogEntriesByCaseInstanceId(String caseInstanceId) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByCaseInstanceId", caseInstanceId);
  }

  public void deleteOperationLogEntriesByCaseDefinitionId(String caseInstanceId) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByCaseDefinitionId", caseInstanceId);
  }

  public void deleteOperationLogEntriesByTaskId(String taskId) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByTaskId", taskId);
  }

  public void deleteOperationLogEntriesByProcessDefinitionId(String processDefinitionId) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByProcessDefinitionId", processDefinitionId);
  }

  public void deleteOperationLogEntriesByProcessDefinitionKey(String processDefinitionKey) {
    getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntriesByProcessDefinitionKey", processDefinitionKey);
  }

  public void deleteOperationLogEntryById(String entryId) {
    if (isHistoryLevelFullEnabled()) {
      getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntryById", entryId);
    }
  }

  public void logUserOperations(UserOperationLogContext context) {
    if (isHistoryLevelFullEnabled()) {
      ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

      HistoryEventProducer eventProducer = configuration.getHistoryEventProducer();
      HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      List<HistoryEvent> historyEvents = eventProducer.createUserOperationLogEvents(context);
      eventHandler.handleEvents(historyEvents);
    }
  }

  public void logTaskOperations(String operation, TaskEntity task, List<PropertyChange> propertyChanges) {
    if (isHistoryLevelFullEnabled()) {
      UserOperationLogContext context = createContextForTask(ENTITY_TYPE_TASK, operation, task, propertyChanges);
      logUserOperations(context);
    }
  }

  public void logLinkOperation(String operation, TaskEntity task, PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {
      UserOperationLogContext context = createContextForTask(ENTITY_TYPE_IDENTITY_LINK, operation, task, Arrays.asList(propertyChange));
      logUserOperations(context);
    }
  }

  public void logProcessInstanceOperation(String operation, String processInstanceId, String processDefinitionId, String processDefinitionKey, PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {

      if(processInstanceId != null) {
        ExecutionEntity instance = getProcessInstanceManager().findExecutionById(processInstanceId);

        if (instance != null) {
          ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) instance.getProcessDefinition();
          processDefinitionId = processDefinition.getId();
          processDefinitionKey = processDefinition.getKey();
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        if (definition != null) {
          processDefinitionKey = definition.getKey();
        }
      }

      UserOperationLogContext context = createContextForProcessInstance(operation, processInstanceId,
        processDefinitionId, processDefinitionKey, Arrays.asList(propertyChange));
      logUserOperations(context);
    }
  }

  public void logProcessDefinitionOperation(String operation, String processDefinitionId, String processDefinitionKey,
      PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {

      if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        if (definition != null) {
          processDefinitionKey = definition.getKey();
        }
      }

      UserOperationLogContext context = createContextForProcessDefinition(
        operation, processDefinitionId, processDefinitionKey, Arrays.asList(propertyChange));

      logUserOperations(context);
    }
  }

  public void logJobOperation(String operation, String jobId, String jobDefinitionId, String processInstanceId,
      String processDefinitionId, String processDefinitionKey,PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {

      if(jobId != null) {
        Job job = getJobManager().findJobById(jobId);
        // Backward compatibility
        if(job != null) {
          jobDefinitionId = job.getJobDefinitionId();
          processInstanceId = job.getProcessInstanceId();
          processDefinitionId = job.getProcessDefinitionId();
          processDefinitionKey = job.getProcessDefinitionKey();
        }
      } else

      if(jobDefinitionId != null) {
        JobDefinitionEntity jobDefinition = getJobDefinitionManager().findById(jobDefinitionId);
        // Backward compatibility
        if(jobDefinition != null) {
          processDefinitionId = jobDefinition.getProcessDefinitionId();
          processDefinitionKey = jobDefinition.getProcessDefinitionKey();
        }
      }
      else if (processInstanceId != null) {
        ExecutionEntity processInstance = getProcessInstanceManager().findExecutionById(processInstanceId);
        // Backward compatibility
        if(processInstance != null) {
          processDefinitionId = processInstance.getProcessDefinitionId();
          processDefinitionKey = ((ProcessDefinitionEntity)processInstance.getProcessDefinition()).getKey();
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        // Backward compatibility
        if(definition != null) {
          processDefinitionKey = definition.getKey();
        }
      }

      UserOperationLogContext context = createContextForJob(operation, jobId, jobDefinitionId, processInstanceId,
        processDefinitionId, processDefinitionKey, Arrays.asList(propertyChange));

      logUserOperations(context);
    }
  }

  public void logJobDefinitionOperation(String operation, String jobDefinitionId, String processDefinitionId,
      String processDefinitionKey, PropertyChange propertyChange) {
    if(isHistoryLevelFullEnabled()) {
      if(jobDefinitionId != null) {
        JobDefinitionEntity jobDefinition = getJobDefinitionManager().findById(jobDefinitionId);
        // Backward compatibility
        if(jobDefinition != null) {
          processDefinitionId = jobDefinition.getProcessDefinitionId();
          processDefinitionKey = jobDefinition.getProcessDefinitionKey();
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        // Backward compatibility
        if(definition != null) {
          processDefinitionKey = definition.getKey();
        }
      }

      UserOperationLogContext context = createContextForJobDefinition(operation, jobDefinitionId,
        processDefinitionId, processDefinitionKey, Arrays.asList(propertyChange));

      logUserOperations(context);
    }
  }

  public void logAttachmentOperation(String operation, TaskEntity task, PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {
      UserOperationLogContext context = createContextForTask(ENTITY_TYPE_ATTACHMENT, operation, task, Arrays.asList(propertyChange));
      logUserOperations(context);
    }
  }

  public void logVariableOperation(String operation, String executionId, String taskId, PropertyChange propertyChange) {
    if(isHistoryLevelFullEnabled()) {

      if (executionId != null) {
        ExecutionEntity execution = getProcessInstanceManager().findExecutionById(executionId);
        logVariableOperation(operation, execution, propertyChange);
      }
      else if (taskId != null) {
        TaskEntity task = getTaskManager().findTaskById(taskId);
        logVariableOperation(operation, task, propertyChange);
      }
    }
  }

  public void logVariableOperation(String operation, TaskEntity task, PropertyChange propertyChange) {
    if(isHistoryLevelFullEnabled()) {
      String processDefinitionKey = null;
      ProcessDefinitionEntity definition = task.getProcessDefinition();
      if (definition != null) {
        processDefinitionKey = definition.getKey();
      }

      UserOperationLogContext context = createContext(EntityTypes.VARIABLE, operation, processDefinitionKey,
        task.getProcessDefinitionId(), task.getProcessInstanceId(), null, null, Arrays.asList(propertyChange));

      context.setTaskId(task.getId());
      context.setExecutionId(task.getExecutionId());

      logUserOperations(context);
    }
  }

  public void logVariableOperation(String operation, ExecutionEntity execution, PropertyChange propertyChange) {
    if(isHistoryLevelFullEnabled()) {
      String processDefinitionKey = null;
      ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
      if (definition != null) {
        processDefinitionKey = definition.getKey();
      }

      UserOperationLogContext context = createContext(EntityTypes.VARIABLE, operation, processDefinitionKey,
        execution.getProcessDefinitionId(), execution.getProcessInstanceId(), null, null, Arrays.asList(propertyChange));

      context.setExecutionId(execution.getId());

      logUserOperations(context);
    }
  }

  protected UserOperationLogContext createContextForTask(String entityType, String operation, TaskEntity task, List<PropertyChange> propertyChanges) {
    UserOperationLogContext context = createContext(entityType, operation);

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(operation)) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }

    context.setPropertyChanges(propertyChanges);

    ProcessDefinitionEntity definition = task.getProcessDefinition();
    if (definition != null) {
      context.setProcessDefinitionKey(definition.getKey());
    }

    context.setProcessDefinitionId(task.getProcessDefinitionId());
    context.setProcessInstanceId(task.getProcessInstanceId());
    context.setExecutionId(task.getExecutionId());
    context.setCaseDefinitionId(task.getCaseDefinitionId());
    context.setCaseInstanceId(task.getCaseInstanceId());
    context.setCaseExecutionId(task.getCaseExecutionId());
    context.setTaskId(task.getId());

    return context;
  }

  protected UserOperationLogContext createContextForProcessDefinition(String operation,
      String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges) {

    return createContext(EntityTypes.PROCESS_DEFINITION, operation, processDefinitionKey,
      processDefinitionId, null, null, null, propertyChanges);
  }

  protected UserOperationLogContext createContextForProcessInstance(String operation,
      String processInstanceId, String processDefinitionId,
      String processDefinitionKey, List<PropertyChange> propertyChanges) {

    return createContext(EntityTypes.PROCESS_INSTANCE, operation, processDefinitionKey,
      processDefinitionId, processInstanceId, null, null, propertyChanges);
  }

  protected UserOperationLogContext createContextForJob(String operation, String jobId, String jobDefinitionId,
      String processInstanceId, String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges) {

    return createContext(EntityTypes.JOB, operation, processDefinitionKey,
      processDefinitionId, processInstanceId, jobDefinitionId, jobId, propertyChanges);
  }

  protected UserOperationLogContext createContextForJobDefinition(String operation, String jobDefinitionId,
      String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges) {

    return createContext(EntityTypes.JOB_DEFINITION, operation, processDefinitionKey, processDefinitionId, null,
      jobDefinitionId, null, propertyChanges);
  }

  protected UserOperationLogContext createContext(String entityType, String operationType) {
    UserOperationLogContext context = new UserOperationLogContext();
    context.setEntityType(entityType);
    context.setOperationType(operationType);

    return context;
  }

  protected UserOperationLogContext createContext(String entityType, String operationType, String processDefinitionKey,
      String processDefinitionId, String processInstanceId, String jobDefinitionId, String jobId,
      List<PropertyChange> propertyChanges) {

    UserOperationLogContext context = createContext(entityType, operationType);

    context.setProcessDefinitionKey(processDefinitionKey);
    context.setProcessDefinitionId(processDefinitionId);
    context.setProcessInstanceId(processInstanceId);
    context.setJobDefinitionId(jobDefinitionId);
    context.setJobId(jobId);
    context.setPropertyChanges(propertyChanges);

    return context;
  }
}
