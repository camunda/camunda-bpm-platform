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

import static org.camunda.bpm.engine.impl.ExternalTaskQueryProperty.CREATE_TIME;
import static org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory.POSTGRES;
import static org.camunda.bpm.engine.impl.util.DatabaseUtil.checkDatabaseType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskManager extends AbstractManager {

  public ExternalTaskEntity findExternalTaskById(String id) {
    return getDbEntityManager().selectById(ExternalTaskEntity.class, id);
  }

  public void insert(ExternalTaskEntity externalTask) {
    getDbEntityManager().insert(externalTask);
    fireExternalTaskAvailableEvent();
  }

  public void delete(ExternalTaskEntity externalTask) {
    getDbEntityManager().delete(externalTask);
  }

  @SuppressWarnings("unchecked")
  public List<ExternalTaskEntity> findExternalTasksByExecutionId(String id) {
    return getDbEntityManager().selectList("selectExternalTasksByExecutionId", id);
  }

  @SuppressWarnings("unchecked")
  public List<ExternalTaskEntity> findExternalTasksByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectExternalTasksByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<ExternalTaskEntity> selectExternalTasksForTopics(Collection<TopicFetchInstruction> queryFilters,
                                                               int maxResults,
                                                               List<QueryOrderingProperty> orderingProperties) {
    if (queryFilters.isEmpty()) {
      return Collections.emptyList();
    }

    boolean shouldApplyOrdering = !orderingProperties.isEmpty();

    Map<String, Object> parameters = Map.of(
        "topics", queryFilters,
        "now", ClockUtil.getCurrentTime(),
        "applyOrdering", shouldApplyOrdering,
        "orderingProperties", orderingProperties,
        "usesPostgres", checkDatabaseType(POSTGRES)
    );

    ListQueryParameterObject parameter = new ListQueryParameterObject(parameters, 0, maxResults);
    configureQuery(parameter);

    DbEntityManager manager = getDbEntityManager();
    return manager.selectList("selectExternalTasksForTopics", parameter);
  }

  @SuppressWarnings("unchecked")
  public List<ExternalTask> findExternalTasksByQueryCriteria(ExternalTaskQueryImpl externalTaskQuery) {
    configureQuery(externalTaskQuery);
    return getDbEntityManager().selectList("selectExternalTaskByQueryCriteria", externalTaskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<String> findExternalTaskIdsByQueryCriteria(ExternalTaskQueryImpl externalTaskQuery) {
    configureQuery(externalTaskQuery);
    return getDbEntityManager().selectList("selectExternalTaskIdsByQueryCriteria", externalTaskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ImmutablePair<String, String>> findDeploymentIdMappingsByQueryCriteria(ExternalTaskQueryImpl externalTaskQuery) {
    configureQuery(externalTaskQuery);
    return getDbEntityManager().selectList("selectExternalTaskDeploymentIdMappingsByQueryCriteria", externalTaskQuery);
  }

  public long findExternalTaskCountByQueryCriteria(ExternalTaskQueryImpl externalTaskQuery) {
    configureQuery(externalTaskQuery);
    return (Long) getDbEntityManager().selectOne("selectExternalTaskCountByQueryCriteria", externalTaskQuery);
  }

  @SuppressWarnings("unchecked")
  public List<String> selectTopicNamesByQuery(ExternalTaskQueryImpl externalTaskQuery) {
    configureQuery(externalTaskQuery);
    return getDbEntityManager().selectList("selectTopicNamesByQuery", externalTaskQuery);
  }

  protected void updateExternalTaskSuspensionState(String processInstanceId,
                                                   String processDefinitionId,
                                                   String processDefinitionKey,
                                                   SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", false);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(ExternalTaskEntity.class, "updateExternalTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateExternalTaskSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    updateExternalTaskSuspensionState(processInstanceId, null, null, suspensionState);
  }

  public void updateExternalTaskSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    updateExternalTaskSuspensionState(null, processDefinitionId, null, suspensionState);
  }

  public void updateExternalTaskSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    updateExternalTaskSuspensionState(null, null, processDefinitionKey, suspensionState);
  }

  public void updateExternalTaskSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String processDefinitionTenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", true);
    parameters.put("processDefinitionTenantId", processDefinitionTenantId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(ExternalTaskEntity.class, "updateExternalTaskSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  protected void configureQuery(ExternalTaskQueryImpl query) {
    getAuthorizationManager().configureExternalTaskQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected void configureQuery(ListQueryParameterObject parameter) {
    getAuthorizationManager().configureExternalTaskFetch(parameter);
    getTenantManager().configureQuery(parameter);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

  protected boolean shouldApplyOrdering(boolean usePriority, boolean useCreateTime) {
    return usePriority || useCreateTime;
  }

  protected boolean useCreateTime(List<QueryOrderingProperty> orderingProperties) {
    return orderingProperties.stream()
        .anyMatch(orderingProperty -> CREATE_TIME.getName().equals(orderingProperty.getQueryProperty().getName()));
  }

  public void fireExternalTaskAvailableEvent() {
    Context.getCommandContext()
        .getTransactionContext()
        .addTransactionListener(TransactionState.COMMITTED, new TransactionListener() {
          @Override
          public void execute(CommandContext commandContext) {
            ProcessEngineImpl.EXT_TASK_CONDITIONS.signalAll();
          }
        });
  }
}


