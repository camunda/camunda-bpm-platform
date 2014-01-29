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

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.UserOperationLogQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;

import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;


/**
 * Manager for {@link UserOperationLogEntryEntity} that also provides a generic and some specific log methods.
 *
 * @author Danny Gräf
 */
public class UserOperationLogManager extends AbstractHistoricManager {

  public long findOperationLogEntryCountByQueryCriteria(UserOperationLogQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectUserOperationLogEntryCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<UserOperationLogEntry> findOperationLogEntriesByQueryCriteria(UserOperationLogQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectUserOperationLogEntriesByQueryCriteria", query, page);
  }

  public void deleteOperationLogEntriesByProcessInstanceId(String historicProcessInstanceId) {
    getDbSqlSession().delete("deleteUserOperationLogEntriesByProcessInstanceId", historicProcessInstanceId);
  }

  public void deleteOperationLogEntriesByTaskId(String taskId) {
    getDbSqlSession().delete("deleteUserOperationLogEntriesByTaskId", taskId);
  }

  public void logOperation(String entityType, String operation, String operationId, String userId, TaskEntity task, PropertyChange propertyChange) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

    if (configuration.getHistoryLevel() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      if (operationId == null) {
        operationId = configuration.getIdGenerator().getNextId();
      }
      final HistoryEventProducer eventProducer = configuration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      HistoryEvent evt = eventProducer.createTaskOperationLogEvt(entityType, userId, operationId, operation, propertyChange, task);
      eventHandler.handleEvent(evt);
    }
  }

  public void logTaskOperation(String operation, String userId, TaskEntity task, Collection<PropertyChange> propertyChanges) {
    if (OPERATION_TYPE_CREATE.equals(operation) && propertyChanges.isEmpty()) {
      logOperation(ENTITY_TYPE_TASK, operation, null, userId, task, PropertyChange.EMPTY_CHANGE);
    } else {
      String operationId = Context.getProcessEngineConfiguration().getIdGenerator().getNextId(); // composite ID
      for (PropertyChange propertyChange : propertyChanges) {
        logOperation(ENTITY_TYPE_TASK, operation, operationId, userId, task, propertyChange);
      }
    }
  }

  public void logLinkOperation(String operation, String userId, TaskEntity task, PropertyChange propertyChange) {
    logOperation(ENTITY_TYPE_IDENTITY_LINK, operation, null, userId, task, propertyChange);
  }

  public void logAttachmentOperation(String operation, String userId, TaskEntity task, PropertyChange propertyChange) {
    logOperation(ENTITY_TYPE_ATTACHMENT, operation, null, userId, task, propertyChange);
  }
}
