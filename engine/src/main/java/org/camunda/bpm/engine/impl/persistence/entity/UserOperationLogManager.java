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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserOperationLogQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntryBuilder;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.util.PermissionConverter;
import org.camunda.bpm.engine.impl.util.StringUtil;

/**
 * Manager for {@link UserOperationLogEntryEventEntity} that also provides a generic and some specific log methods.
 *
 * @author Danny Gräf
 * @author Tobias Metzke
 */
public class UserOperationLogManager extends AbstractHistoricManager {

  public UserOperationLogEntry findOperationLogById(String entryId) {
    return getDbEntityManager().selectById(UserOperationLogEntryEventEntity.class, entryId);
  }

  public UserOperationLogEntry findOperationLogByOperationId(String operationId) {
    List<?> list = getDbEntityManager().selectList("selectUserOperationLogByOperationId", operationId, 0, 1);
    if (list!=null && !list.isEmpty()) {
      return (UserOperationLogEntry) list.get(0);
    }
    return null;
  }

  public long findOperationLogEntryCountByQueryCriteria(UserOperationLogQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectUserOperationLogEntryCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<UserOperationLogEntry> findOperationLogEntriesByQueryCriteria(UserOperationLogQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectUserOperationLogEntriesByQueryCriteria", query, page);
  }

  public DbOperation addRemovalTimeToUserOperationLogByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(UserOperationLogEntryEventEntity.class, "updateUserOperationLogByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToUserOperationLogByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(UserOperationLogEntryEventEntity.class, "updateUserOperationLogByProcessInstanceId", parameters);
  }

  public void updateOperationLogAnnotationByOperationId(String operationId, String annotation) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("operationId", operationId);
    parameters.put("annotation", annotation);

    getDbEntityManager()
        .updatePreserveOrder(UserOperationLogEntryEventEntity.class, "updateOperationLogAnnotationByOperationId", parameters);
  }

  public void deleteOperationLogEntryById(String entryId) {
    if (isHistoryEventProduced()) {
      getDbEntityManager().delete(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogEntryById", entryId);
    }
  }

  public DbOperation deleteOperationLogByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(UserOperationLogEntryEventEntity.class, "deleteUserOperationLogByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

  public void logUserOperations(UserOperationLogContext context) {
    if (isUserOperationLogEnabled()) {
      fireUserOperationLog(context);
    }
  }

  public void logUserOperation(IdentityOperationResult operationResult, String userId) {
    logUserOperation(getOperationType(operationResult), userId);
  }

  public void logUserOperation(String operation, String userId) {
    if (operation != null && isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.USER)
            .category(UserOperationLogEntry.CATEGORY_ADMIN)
            .propertyChanges(new PropertyChange("userId", null, userId));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logGroupOperation(IdentityOperationResult operationResult, String groupId) {
    logGroupOperation(getOperationType(operationResult), groupId);
  }

  public void logGroupOperation(String operation, String groupId) {
    if (operation != null && isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.GROUP)
            .category(UserOperationLogEntry.CATEGORY_ADMIN)
            .propertyChanges(new PropertyChange("groupId", null, groupId));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logTenantOperation(IdentityOperationResult operationResult, String tenantId) {
    logTenantOperation(getOperationType(operationResult), tenantId);
  }

  public void logTenantOperation(String operation, String tenantId) {
    if (operation != null && isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.TENANT)
            .category(UserOperationLogEntry.CATEGORY_ADMIN)
            .tenantId(tenantId)
            .propertyChanges(new PropertyChange("tenantId", null, tenantId));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logMembershipOperation(IdentityOperationResult operationResult, String userId, String groupId, String tenantId) {
    logMembershipOperation(getOperationType(operationResult), userId, groupId, tenantId);
  }

  public void logMembershipOperation(String operation, String userId, String groupId, String tenantId) {
    if (operation != null && isUserOperationLogEnabled()) {
      String entityType = tenantId == null ? EntityTypes.GROUP_MEMBERSHIP : EntityTypes.TENANT_MEMBERSHIP;

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, entityType)
            .tenantId(tenantId)
            .category(UserOperationLogEntry.CATEGORY_ADMIN);
      List<PropertyChange> propertyChanges = new ArrayList<>();
      if (userId != null) {
        propertyChanges.add(new PropertyChange("userId", null, userId));
      }
      if (groupId != null) {
        propertyChanges.add(new PropertyChange("groupId", null, groupId));
      }
      if (tenantId != null) {
        propertyChanges.add(new PropertyChange("tenantId", null, tenantId));
      }
      entryBuilder.propertyChanges(propertyChanges);


      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logTaskOperations(String operation, TaskEntity task, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.TASK)
            .category(UserOperationLogEntry.CATEGORY_TASK_WORKER)
            .inContextOf(task, propertyChanges);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logTaskOperations(String operation, HistoricTaskInstance historicTask, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.TASK)
          .inContextOf(historicTask, propertyChanges)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logLinkOperation(String operation, TaskEntity task, PropertyChange propertyChange) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.IDENTITY_LINK)
            .category(UserOperationLogEntry.CATEGORY_TASK_WORKER)
            .inContextOf(task, Arrays.asList(propertyChange));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logProcessInstanceOperation(String operation, List<PropertyChange> propertyChanges) {
    logProcessInstanceOperation(operation, null, null, null, propertyChanges);
  }

  public void logProcessInstanceOperation(String operation, String processInstanceId, String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges) {
    logProcessInstanceOperation(operation, processInstanceId, processDefinitionId, processDefinitionKey, propertyChanges, null);
  }

  public void logProcessInstanceOperation(String operation, String processInstanceId, String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges, String annotation) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =  UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.PROCESS_INSTANCE)
            .propertyChanges(propertyChanges)
            .processInstanceId(processInstanceId)
            .processDefinitionId(processDefinitionId)
            .processDefinitionKey(processDefinitionKey)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);
      if (annotation != null) {
        entryBuilder.annotation(annotation);
      }

      if(processInstanceId != null) {
        ExecutionEntity instance = getProcessInstanceManager().findExecutionById(processInstanceId);

        if (instance != null) {
          entryBuilder.inContextOf(instance);
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        if (definition != null) {
          entryBuilder.inContextOf(definition);
        }
      }

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logProcessDefinitionOperation(String operation, String processDefinitionId, String processDefinitionKey,
                                            PropertyChange propertyChange) {
    logProcessDefinitionOperation(
      operation,
      processDefinitionId,
      processDefinitionKey,
      Collections.singletonList(propertyChange)
    );
  }

  public void logProcessDefinitionOperation(String operation, String processDefinitionId, String processDefinitionKey,
                                            List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.PROCESS_DEFINITION)
          .propertyChanges(propertyChanges)
          .processDefinitionId(processDefinitionId)
          .processDefinitionKey(processDefinitionKey)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        entryBuilder.inContextOf(definition);
      }

      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logCaseInstanceOperation(String operation, String caseInstanceId, String tenantId, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.CASE_INSTANCE)
          .caseInstanceId(caseInstanceId)
          .propertyChanges(propertyChanges)
          .tenantId(tenantId)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logCaseDefinitionOperation(String operation, String caseDefinitionId, String tenantId, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.CASE_DEFINITION)
          .propertyChanges(propertyChanges)
          .caseDefinitionId(caseDefinitionId)
          .tenantId(tenantId)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logDecisionDefinitionOperation(String operation, String tenantId, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.DECISION_DEFINITION)
          .propertyChanges(propertyChanges)
          .tenantId(tenantId)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logJobOperation(String operation, String jobId, String jobDefinitionId, String processInstanceId,
      String processDefinitionId, String processDefinitionKey, PropertyChange propertyChange) {
    logJobOperation(operation, jobId, jobDefinitionId, processInstanceId, processDefinitionId, processDefinitionKey,
        Collections.singletonList(propertyChange));
  }

  public void logJobOperation(String operation, String jobId, String jobDefinitionId, String processInstanceId,
      String processDefinitionId, String processDefinitionKey, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.JOB)
            .jobId(jobId)
            .jobDefinitionId(jobDefinitionId)
            .processDefinitionId(processDefinitionId)
            .processDefinitionKey(processDefinitionKey)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      if(jobId != null) {
        JobEntity job = getJobManager().findJobById(jobId);
        // Backward compatibility
        if(job != null) {
          entryBuilder.inContextOf(job);
        }
      } else

      if(jobDefinitionId != null) {
        JobDefinitionEntity jobDefinition = getJobDefinitionManager().findById(jobDefinitionId);
        // Backward compatibility
        if(jobDefinition != null) {
          entryBuilder.inContextOf(jobDefinition);
        }
      }
      else if (processInstanceId != null) {
        ExecutionEntity processInstance = getProcessInstanceManager().findExecutionById(processInstanceId);
        // Backward compatibility
        if(processInstance != null) {
          entryBuilder.inContextOf(processInstance);
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        // Backward compatibility
        if(definition != null) {
          entryBuilder.inContextOf(definition);
        }
      }

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logJobDefinitionOperation(String operation, String jobDefinitionId, String processDefinitionId,
      String processDefinitionKey, PropertyChange propertyChange) {
    if(isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.JOB_DEFINITION)
            .jobDefinitionId(jobDefinitionId)
            .processDefinitionId(processDefinitionId)
            .processDefinitionKey(processDefinitionKey)
            .propertyChanges(propertyChange)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      if(jobDefinitionId != null) {
        JobDefinitionEntity jobDefinition = getJobDefinitionManager().findById(jobDefinitionId);
        // Backward compatibility
        if(jobDefinition != null) {
          entryBuilder.inContextOf(jobDefinition);
        }
      }
      else if (processDefinitionId != null) {
        ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
        // Backward compatibility
        if(definition != null) {
          entryBuilder.inContextOf(definition);
        }
      }

      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logAttachmentOperation(String operation, TaskEntity task, PropertyChange propertyChange) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.ATTACHMENT)
            .category(UserOperationLogEntry.CATEGORY_TASK_WORKER)
            .inContextOf(task, Arrays.asList(propertyChange));
      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logAttachmentOperation(String operation, ExecutionEntity processInstance, PropertyChange propertyChange) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.ATTACHMENT)
            .category(UserOperationLogEntry.CATEGORY_TASK_WORKER)
            .inContextOf(processInstance, Arrays.asList(propertyChange));
      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logVariableOperation(String operation, String executionId, String taskId, PropertyChange propertyChange) {
    if(isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.VARIABLE)
          .propertyChanges(propertyChange);

      if (executionId != null) {
        ExecutionEntity execution = getProcessInstanceManager().findExecutionById(executionId);
        entryBuilder.inContextOf(execution)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);
      }
      else if (taskId != null) {
        TaskEntity task = getTaskManager().findTaskById(taskId);
        entryBuilder.inContextOf(task, Arrays.asList(propertyChange))
          .category(UserOperationLogEntry.CATEGORY_TASK_WORKER);
      }

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logHistoricVariableOperation(String operation, HistoricProcessInstanceEntity historicProcessInstance, ResourceDefinitionEntity<?> definition, PropertyChange propertyChange) {
    if(isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.VARIABLE)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR)
            .propertyChanges(propertyChange)
            .inContextOf(historicProcessInstance, definition, Arrays.asList(propertyChange));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logHistoricVariableOperation(String operation, HistoricVariableInstanceEntity historicVariableInstance, ResourceDefinitionEntity<?> definition, PropertyChange propertyChange) {
    if(isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.VARIABLE)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR)
            .propertyChanges(propertyChange)
            .inContextOf(historicVariableInstance, definition, Arrays.asList(propertyChange));

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logDeploymentOperation(String operation, String deploymentId, String tenantId, List<PropertyChange> propertyChanges) {
    if(isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.DEPLOYMENT)
            .deploymentId(deploymentId)
            .tenantId(tenantId)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }

  }

  public void logBatchOperation(String operation, String tenantId, List<PropertyChange> propertyChange) {
    logBatchOperation(operation, null, tenantId, propertyChange);
  }

  public void logBatchOperation(String operation, String batchId, String tenantId, PropertyChange propertyChange) {
    logBatchOperation(operation, batchId, tenantId, Collections.singletonList(propertyChange));
  }

  public void logBatchOperation(String operation, String batchId, String tenantId, List<PropertyChange> propertyChanges) {
    if(isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.BATCH)
          .batchId(batchId)
          .tenantId(tenantId)
          .propertyChanges(propertyChanges)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logDecisionInstanceOperation(String operation, String tenantId, List<PropertyChange> propertyChanges) {
    if(isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
        UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.DECISION_INSTANCE)
          .propertyChanges(propertyChanges)
          .tenantId(tenantId)
          .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      context.addEntry(entryBuilder.create());

      fireUserOperationLog(context);
    }
  }

  public void logExternalTaskOperation(String operation, ExternalTaskEntity externalTask, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.EXTERNAL_TASK)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      if (externalTask != null) {
        ExecutionEntity instance = null;
        ProcessDefinitionEntity definition = null;
        if (externalTask.getProcessInstanceId() != null) {
          instance = getProcessInstanceManager().findExecutionById(externalTask.getProcessInstanceId());
        } else if (externalTask.getProcessDefinitionId() != null) {
          definition = getProcessDefinitionManager().findLatestProcessDefinitionById(externalTask.getProcessDefinitionId());
        }
        entryBuilder.processInstanceId(externalTask.getProcessInstanceId())
          .processDefinitionId(externalTask.getProcessDefinitionId())
          .processDefinitionKey(externalTask.getProcessDefinitionKey())
          .inContextOf(externalTask, instance, definition);
      }

      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logMetricsOperation(String operation, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.METRICS)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);
      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logTaskMetricsOperation(String operation, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.TASK_METRICS)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_OPERATOR);
      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logFilterOperation(String operation, String filterId) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.FILTER)
            .propertyChanges(new PropertyChange("filterId", null, filterId))
            .category(UserOperationLogEntry.CATEGORY_TASK_WORKER);
      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logPropertyOperation(String operation, List<PropertyChange> propertyChanges) {
    if (isUserOperationLogEnabled()) {
      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.PROPERTY)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_ADMIN);
      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  public void logSetAnnotationOperation(String operationId, String tenantId) {
    logAnnotationOperation(operationId, EntityTypes.OPERATION_LOG, "operationId", UserOperationLogEntry.OPERATION_TYPE_SET_ANNOTATION, tenantId);
  }

  public void logClearAnnotationOperation(String operationId, String tenantId) {
    logAnnotationOperation(operationId, EntityTypes.OPERATION_LOG, "operationId", UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION, tenantId);
  }

  public void logSetIncidentAnnotationOperation(String incidentId, String tenantId) {
    logAnnotationOperation(incidentId, EntityTypes.INCIDENT, "incidentId", UserOperationLogEntry.OPERATION_TYPE_SET_ANNOTATION, tenantId);
  }

  public void logClearIncidentAnnotationOperation(String incidentId, String tenantId) {
    logAnnotationOperation(incidentId, EntityTypes.INCIDENT, "incidentId", UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION, tenantId);
  }

  protected void logAnnotationOperation(String id, String type, String idProperty, String operationType, String tenantId) {
    if (isUserOperationLogEnabled()) {

      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operationType, type)
              .propertyChanges(new PropertyChange(idProperty, null, id))
              .tenantId(tenantId)
              .category(UserOperationLogEntry.CATEGORY_OPERATOR);

      UserOperationLogContext context = new UserOperationLogContext();
      context.addEntry(entryBuilder.create());


      fireUserOperationLog(context);
    }
  }

  public void logAuthorizationOperation(String operation, AuthorizationEntity authorization, AuthorizationEntity previousValues) {
    if (isUserOperationLogEnabled()) {
      List<PropertyChange> propertyChanges = new ArrayList<>();
      propertyChanges.add(new PropertyChange("permissionBits", previousValues == null ? null : previousValues.getPermissions(), authorization.getPermissions()));
      propertyChanges.add(new PropertyChange("permissions", previousValues == null ? null : getPermissionStringList(previousValues), getPermissionStringList(authorization)));
      propertyChanges.add(new PropertyChange("type", previousValues == null ? null : previousValues.getAuthorizationType(), authorization.getAuthorizationType()));
      propertyChanges.add(new PropertyChange("resource", previousValues == null ? null : getResourceName(previousValues.getResourceType()), getResourceName(authorization.getResourceType())));
      propertyChanges.add(new PropertyChange("resourceId", previousValues == null ? null : previousValues.getResourceId(), authorization.getResourceId()));
      if (authorization.getUserId() != null || (previousValues != null && previousValues.getUserId() != null)) {
        propertyChanges.add(new PropertyChange("userId", previousValues == null ? null : previousValues.getUserId(), authorization.getUserId()));
      }
      if (authorization.getGroupId() != null || (previousValues != null && previousValues.getGroupId() != null)) {
        propertyChanges.add(new PropertyChange("groupId", previousValues == null ? null : previousValues.getGroupId(), authorization.getGroupId()));
      }

      UserOperationLogContext context = new UserOperationLogContext();
      UserOperationLogContextEntryBuilder entryBuilder =
          UserOperationLogContextEntryBuilder.entry(operation, EntityTypes.AUTHORIZATION)
            .propertyChanges(propertyChanges)
            .category(UserOperationLogEntry.CATEGORY_ADMIN);
      context.addEntry(entryBuilder.create());
      fireUserOperationLog(context);
    }
  }

  protected String getPermissionStringList(AuthorizationEntity authorization) {
    Permission[] permissionsForResource = Context.getProcessEngineConfiguration().getPermissionProvider().getPermissionsForResource(authorization.getResourceType());
    Permission[] permissions = authorization.getPermissions(permissionsForResource);
    String[] namesForPermissions = PermissionConverter.getNamesForPermissions(authorization, permissions);
    if (namesForPermissions.length == 0) {
      return Permissions.NONE.getName();
    }
    return StringUtil.trimToMaximumLengthAllowed(StringUtil.join(Arrays.asList(namesForPermissions).iterator()));
  }

  protected String getResourceName(int resourceType) {
    return Context.getProcessEngineConfiguration().getPermissionProvider().getNameForResource(resourceType);
  }

  public boolean isUserOperationLogEnabled() {
    return isHistoryEventProduced() &&
        ((isUserOperationLogEnabledOnCommandContext() && isUserAuthenticated()) ||
            !writeUserOperationLogOnlyWithLoggedInUser());
  }

  protected boolean isHistoryEventProduced() {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    return historyLevel.isHistoryEventProduced(HistoryEventTypes.USER_OPERATION_LOG, null);
  }

  protected boolean isUserAuthenticated() {
    String userId = getAuthenticatedUserId();
    return userId != null && !userId.isEmpty();
  }

  protected String getAuthenticatedUserId() {
    CommandContext commandContext = Context.getCommandContext();
    return commandContext.getAuthenticatedUserId();
  }

  protected void fireUserOperationLog(final UserOperationLogContext context) {
    if (context.getUserId() == null) {
      context.setUserId(getAuthenticatedUserId());
    }

    HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
      @Override
      public List<HistoryEvent> createHistoryEvents(HistoryEventProducer producer) {
        return producer.createUserOperationLogEvents(context);
      }
    });
  }

  protected boolean writeUserOperationLogOnlyWithLoggedInUser() {
    return Context.getCommandContext().isRestrictUserOperationLogToAuthenticatedUsers();
  }

  protected boolean isUserOperationLogEnabledOnCommandContext() {
    return Context.getCommandContext().isUserOperationLogEnabled();
  }

  protected String getOperationType(IdentityOperationResult operationResult) {
    switch (operationResult.getOperation()) {
    case IdentityOperationResult.OPERATION_CREATE:
      return UserOperationLogEntry.OPERATION_TYPE_CREATE;
    case IdentityOperationResult.OPERATION_UPDATE:
      return UserOperationLogEntry.OPERATION_TYPE_UPDATE;
    case IdentityOperationResult.OPERATION_DELETE:
      return UserOperationLogEntry.OPERATION_TYPE_DELETE;
    case IdentityOperationResult.OPERATION_UNLOCK:
      return UserOperationLogEntry.OPERATION_TYPE_UNLOCK;
    default:
      return null;
    }
  }

  protected void configureQuery(UserOperationLogQueryImpl query) {
    getAuthorizationManager().configureUserOperationLogQuery(query);
    getTenantManager().configureQuery(query);
  }

}
