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

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;

/**
 * Manager for {@link UserOperationLogEntryEventEntity} that also provides a generic and some specific log methods.
 *
 * @author Danny Gräf
 */
public class UserOperationLogManager extends AbstractHistoricManager {

  public long findOperationLogEntryCountByQueryCriteria(UserOperationLogQueryImpl query) {
    return (Long) getDbEntityManager().selectOne("selectUserOperationLogEntryCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<UserOperationLogEntry> findOperationLogEntriesByQueryCriteria(UserOperationLogQueryImpl query, Page page) {
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

  /**
   * The parameters processInstanceId, processDefinitionId and processInstanceKey are interpreted as selection constraints
   * that are affected by the operation.
   */
  public void logProcessInstanceOperation(String operation, String processInstanceId,
      String processDefinitionId, String processDefinitionKey, PropertyChange propertyChange) {
    if (isHistoryLevelFullEnabled()) {
      UserOperationLogContext context = createContextForProcessInstance(
          operation, processInstanceId,
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

  protected UserOperationLogContext createContextForTask(String entityType, String operation, TaskEntity task, List<PropertyChange> propertyChanges) {
    UserOperationLogContext context = new UserOperationLogContext();

    context.setEntityType(entityType);
    context.setOperationType(operation);

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(operation)) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    context.setPropertyChanges(propertyChanges);

    context.setProcessDefinitionId(task.getProcessDefinitionId());
    context.setProcessInstanceId(task.getProcessInstanceId());
    context.setExecutionId(task.getExecutionId());
    context.setCaseDefinitionId(task.getCaseDefinitionId());
    context.setCaseInstanceId(task.getCaseInstanceId());
    context.setCaseExecutionId(task.getCaseExecutionId());
    context.setTaskId(task.getId());

    return context;
  }

  protected UserOperationLogContext createContextForProcessInstance(String operation,
      String processInstanceId, String processDefinitionId,
      String processDefinitionKey, List<PropertyChange> propertyChanges) {
    UserOperationLogContext context = new UserOperationLogContext();

    context.setEntityType(EntityTypes.PROCESS_INSTANCE);
    context.setOperationType(operation);

    context.setProcessInstanceId(processInstanceId);
    context.setProcessDefinitionId(processDefinitionId);
    context.setProcessDefinitionKey(processDefinitionKey);
    context.setPropertyChanges(propertyChanges);

    return context;
  }
}
