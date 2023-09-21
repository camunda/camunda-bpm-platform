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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.CleanableHistoricProcessInstanceReportImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIdentityLinkLogEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;

/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceManager extends AbstractHistoricManager {

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricProcessInstanceEntity.class, processInstanceId);
    }
    return null;
  }

  public HistoricProcessInstanceEventEntity findHistoricProcessInstanceEvent(String eventId) {
    if (isHistoryEnabled()) {
      return getDbEntityManager().selectById(HistoricProcessInstanceEventEntity.class, eventId);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (isHistoryEnabled()) {
      List<String> historicProcessInstanceIds = getDbEntityManager()
        .selectList("selectHistoricProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

      if (!historicProcessInstanceIds.isEmpty()) {
        deleteHistoricProcessInstanceByIds(historicProcessInstanceIds);
      }
    }
  }

  public void deleteHistoricProcessInstanceByIds(List<String> processInstanceIds) {
    if (isHistoryEnabled()) {
      CommandContext commandContext = getCommandContext();

      // break down parameter list to not hit query parameter limitations
      List<List<String>> partitions = CollectionUtil.partition(processInstanceIds, DbSqlSessionFactory.MAXIMUM_NUMBER_PARAMS);
      for (List<String> partition : partitions) {
        commandContext.getHistoricDetailManager().deleteHistoricDetailsByProcessInstanceIds(partition);
        commandContext.getHistoricVariableInstanceManager().deleteHistoricVariableInstanceByProcessInstanceIds(partition);
        commandContext.getCommentManager().deleteCommentsByProcessInstanceIds(partition);
        commandContext.getAttachmentManager().deleteAttachmentsByProcessInstanceIds(partition);
        commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstancesByProcessInstanceIds(partition, false);
        commandContext.getHistoricActivityInstanceManager().deleteHistoricActivityInstancesByProcessInstanceIds(partition);
        commandContext.getHistoricIncidentManager().deleteHistoricIncidentsByProcessInstanceIds(partition);
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByProcessInstanceIds(partition);
        commandContext.getHistoricExternalTaskLogManager().deleteHistoricExternalTaskLogsByProcessInstanceIds(partition);
        commandContext.getAuthorizationManager().deleteAuthorizationsByResourceIds(Resources.HISTORIC_PROCESS_INSTANCE, partition);

        commandContext.getDbEntityManager().deletePreserveOrder(HistoricProcessInstanceEntity.class, "deleteHistoricProcessInstances", partition);
      }
    }
  }

  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (isHistoryEnabled()) {
      configureQuery(historicProcessInstanceQuery);
      return (Long) getDbEntityManager().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    if (isHistoryEnabled()) {
      configureQuery(historicProcessInstanceQuery);
      return getDbEntityManager().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricProcessInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricProcessInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricProcessInstanceCountByNativeQuery", parameterMap);
  }

  @SuppressWarnings("unchecked")
  public List<String> findHistoricProcessInstanceIdsForCleanup(Integer batchSize, int minuteFrom, int minuteTo) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("currentTimestamp", ClockUtil.getCurrentTime());
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    ListQueryParameterObject parameterObject = new ListQueryParameterObject(parameters, 0, batchSize);
    return getDbEntityManager().selectList("selectHistoricProcessInstanceIdsForCleanup", parameterObject);
  }

  @SuppressWarnings("unchecked")
  public List<String> findHistoricProcessInstanceIds(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    configureQuery(historicProcessInstanceQuery);
    return getDbEntityManager().selectList("selectHistoricProcessInstanceIdsByQueryCriteria", historicProcessInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ImmutablePair<String, String>> findDeploymentIdMappingsByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    configureQuery(historicProcessInstanceQuery);
    return getDbEntityManager().selectList("selectHistoricProcessInstanceDeploymentIdMappingsByQueryCriteria", historicProcessInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<CleanableHistoricProcessInstanceReportResult> findCleanableHistoricProcessInstancesReportByCriteria(CleanableHistoricProcessInstanceReportImpl query, Page page) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());

    getAuthorizationManager().configureQueryHistoricFinishedInstanceReport(query, Resources.PROCESS_DEFINITION);
    getTenantManager().configureQuery(query);
    return getDbEntityManager().selectList("selectFinishedProcessInstancesReportEntities", query, page);
  }

  public long findCleanableHistoricProcessInstancesReportCountByCriteria(CleanableHistoricProcessInstanceReportImpl query) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());

    getAuthorizationManager().configureQueryHistoricFinishedInstanceReport(query, Resources.PROCESS_DEFINITION);
    getTenantManager().configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectFinishedProcessInstancesReportEntitiesCount", query);
  }

  public void addRemovalTimeToProcessInstancesByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime) {
    addRemovalTimeToProcessInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime, null, Collections.emptySet());
  }

  public Map<Class<? extends DbEntity>, DbOperation> addRemovalTimeToProcessInstancesByRootProcessInstanceId(
      String rootProcessInstanceId,
      Date removalTime,
      Integer batchSize,
      Set<String> entities) {

    Map<Class<? extends DbEntity>, DbOperation> updateOperations = new HashMap<>();
    CommandContext commandContext = getCommandContext();

    if (isPerformUpdate(entities, HistoricActivityInstanceEventEntity.class)) {
      addOperation(commandContext.getHistoricActivityInstanceManager()
          .addRemovalTimeToActivityInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricTaskInstanceEventEntity.class)) {
      addOperation(commandContext.getHistoricTaskInstanceManager()
          .addRemovalTimeToTaskInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricVariableInstanceEntity.class)) {
      addOperation(commandContext.getHistoricVariableInstanceManager()
          .addRemovalTimeToVariableInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricDetailEventEntity.class)) {
      addOperation(commandContext.getHistoricDetailManager()
          .addRemovalTimeToDetailsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricIncidentEventEntity.class)) {
      addOperation(commandContext.getHistoricIncidentManager()
          .addRemovalTimeToIncidentsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricExternalTaskLogEntity.class)) {
      addOperation(commandContext.getHistoricExternalTaskLogManager()
          .addRemovalTimeToExternalTaskLogByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricJobLogEventEntity.class)) {
      addOperation(commandContext.getHistoricJobLogManager()
          .addRemovalTimeToJobLogByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, UserOperationLogEntryEventEntity.class)) {
      addOperation(commandContext.getOperationLogManager()
          .addRemovalTimeToUserOperationLogByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricIdentityLinkLogEventEntity.class)) {
      addOperation(commandContext.getHistoricIdentityLinkManager()
          .addRemovalTimeToIdentityLinkLogByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, CommentEntity.class)) {
      addOperation(commandContext.getCommentManager()
          .addRemovalTimeToCommentsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, AttachmentEntity.class)) {
      addOperation(commandContext.getAttachmentManager()
          .addRemovalTimeToAttachmentsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, ByteArrayEntity.class)) {
      addOperation(commandContext.getByteArrayManager()
          .addRemovalTimeToByteArraysByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isEnableHistoricInstancePermissions() && isPerformUpdate(entities, AuthorizationEntity.class)) {
      addOperation(commandContext.getAuthorizationManager()
          .addRemovalTimeToAuthorizationsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (batchSize == null || isPerformUpdateOnly(entities, HistoricProcessInstanceEventEntity.class)) {
      // update process instance last if updated in batches to avoid orphans
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("rootProcessInstanceId", rootProcessInstanceId);
      parameters.put("removalTime", removalTime);
      parameters.put("maxResults", batchSize);

      addOperation(getDbEntityManager().updatePreserveOrder(HistoricProcessInstanceEventEntity.class,
          "updateHistoricProcessInstanceEventsByRootProcessInstanceId", parameters), updateOperations);
    }

    return updateOperations;
  }

  public Map<Class<? extends DbEntity>, DbOperation> addRemovalTimeById(String processInstanceId,
      Date removalTime,
      Integer batchSize,
      Set<String> entities) {

    Map<Class<? extends DbEntity>, DbOperation> updateOperations = new HashMap<>();
    CommandContext commandContext = getCommandContext();

    if (isPerformUpdate(entities, HistoricActivityInstanceEventEntity.class)) {
      addOperation(commandContext.getHistoricActivityInstanceManager()
          .addRemovalTimeToActivityInstancesByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricTaskInstanceEventEntity.class)) {
      addOperation(commandContext.getHistoricTaskInstanceManager()
          .addRemovalTimeToTaskInstancesByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricVariableInstanceEntity.class)) {
      addOperation(commandContext.getHistoricVariableInstanceManager()
          .addRemovalTimeToVariableInstancesByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricDetailEventEntity.class)) {
      addOperation(commandContext.getHistoricDetailManager()
          .addRemovalTimeToDetailsByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricIncidentEventEntity.class)) {
      addOperation(commandContext.getHistoricIncidentManager()
          .addRemovalTimeToIncidentsByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricExternalTaskLogEntity.class)) {
      addOperation(commandContext.getHistoricExternalTaskLogManager()
          .addRemovalTimeToExternalTaskLogByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricJobLogEventEntity.class)) {
      addOperation(commandContext.getHistoricJobLogManager()
          .addRemovalTimeToJobLogByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, UserOperationLogEntryEventEntity.class)) {
      addOperation(commandContext.getOperationLogManager()
          .addRemovalTimeToUserOperationLogByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, HistoricIdentityLinkLogEventEntity.class)) {
      addOperation(commandContext.getHistoricIdentityLinkManager()
          .addRemovalTimeToIdentityLinkLogByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, CommentEntity.class)) {
      addOperation(commandContext.getCommentManager()
          .addRemovalTimeToCommentsByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, AttachmentEntity.class)) {
      addOperation(commandContext.getAttachmentManager()
          .addRemovalTimeToAttachmentsByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isPerformUpdate(entities, ByteArrayEntity.class)) {
      addOperation(commandContext.getByteArrayManager()
          .addRemovalTimeToByteArraysByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (isEnableHistoricInstancePermissions() && isPerformUpdate(entities, AuthorizationEntity.class)) {
      addOperation(commandContext.getAuthorizationManager()
          .addRemovalTimeToAuthorizationsByProcessInstanceId(processInstanceId, removalTime, batchSize),
          updateOperations);
    }

    if (batchSize == null || isPerformUpdateOnly(entities, HistoricProcessInstanceEventEntity.class)) {
      // update process instance last if updated in batches to avoid orphans
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("processInstanceId", processInstanceId);
      parameters.put("removalTime", removalTime);
      parameters.put("maxResults", batchSize);

      addOperation(getDbEntityManager().updatePreserveOrder(HistoricProcessInstanceEventEntity.class,
          "updateHistoricProcessInstanceByProcessInstanceId", parameters), updateOperations);
    }

    return updateOperations;
  }

  public Map<Class<? extends DbEntity>, DbOperation> deleteHistoricProcessInstancesByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    CommandContext commandContext = getCommandContext();

    Map<Class<? extends DbEntity>, DbOperation> deleteOperations = new HashMap<>();

    DbOperation deleteActivityInstances = commandContext.getHistoricActivityInstanceManager()
      .deleteHistoricActivityInstancesByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteActivityInstances.getEntityType(), deleteActivityInstances);

    DbOperation deleteTaskInstances = commandContext.getHistoricTaskInstanceManager()
      .deleteHistoricTaskInstancesByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteTaskInstances.getEntityType(), deleteTaskInstances);

    DbOperation deleteVariableInstances = commandContext.getHistoricVariableInstanceManager()
      .deleteHistoricVariableInstancesByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteVariableInstances.getEntityType(), deleteVariableInstances);

    DbOperation deleteDetails = commandContext.getHistoricDetailManager()
      .deleteHistoricDetailsByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteDetails.getEntityType(), deleteDetails);

    DbOperation deleteIncidents = commandContext.getHistoricIncidentManager()
      .deleteHistoricIncidentsByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteIncidents.getEntityType(), deleteIncidents);

    DbOperation deleteTaskLog = commandContext.getHistoricExternalTaskLogManager()
      .deleteExternalTaskLogByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteTaskLog.getEntityType(), deleteTaskLog);

    DbOperation deleteJobLog = commandContext.getHistoricJobLogManager()
      .deleteJobLogByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteJobLog.getEntityType(), deleteJobLog);

    DbOperation deleteOperationLog = commandContext.getOperationLogManager()
      .deleteOperationLogByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteOperationLog.getEntityType(), deleteOperationLog);

    DbOperation deleteIdentityLinkLog = commandContext.getHistoricIdentityLinkManager()
      .deleteHistoricIdentityLinkLogByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteIdentityLinkLog.getEntityType(), deleteIdentityLinkLog);

    DbOperation deleteComments = commandContext.getCommentManager()
      .deleteCommentsByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteComments.getEntityType(), deleteComments);

    DbOperation deleteAttachments = commandContext.getAttachmentManager()
      .deleteAttachmentsByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteAttachments.getEntityType(), deleteAttachments);

    DbOperation deleteByteArrays = commandContext.getByteArrayManager()
      .deleteByteArraysByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteByteArrays.getEntityType(), deleteByteArrays);

    DbOperation deleteAuthorizations = commandContext.getAuthorizationManager()
        .deleteAuthorizationsByRemovalTime(removalTime, minuteFrom, minuteTo, batchSize);

    deleteOperations.put(deleteAuthorizations.getEntityType(), deleteAuthorizations);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    DbOperation deleteProcessInstances = getDbEntityManager()
      .deletePreserveOrder(HistoricProcessInstanceEntity.class, "deleteHistoricProcessInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));

    deleteOperations.put(deleteProcessInstances.getEntityType(), deleteProcessInstances);

    return deleteOperations;
  }

  protected void configureQuery(HistoricProcessInstanceQueryImpl query) {
    getAuthorizationManager().configureHistoricProcessInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected static boolean isEnableHistoricInstancePermissions() {
    return Context.getProcessEngineConfiguration()
        .isEnableHistoricInstancePermissions();
  }
}
