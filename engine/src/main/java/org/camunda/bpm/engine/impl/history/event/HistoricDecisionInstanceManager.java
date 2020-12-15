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
package org.camunda.bpm.engine.impl.history.event;

import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.impl.CleanableHistoricDecisionInstanceReportImpl;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ImmutablePair;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data base operations for {@link HistoricDecisionInstanceEntity}.
 *
 * @author Philipp Ossler
 */
public class HistoricDecisionInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricDecisionInstancesByDecisionDefinitionId(String decisionDefinitionId) {
    if (isHistoryEnabled()) {
      List<HistoricDecisionInstanceEntity> decisionInstances = findHistoricDecisionInstancesByDecisionDefinitionId(decisionDefinitionId);

      List<String> decisionInstanceIds = new ArrayList<>();
      for(HistoricDecisionInstanceEntity decisionInstance : decisionInstances) {
        decisionInstanceIds.add(decisionInstance.getId());
        // delete decision instance
        decisionInstance.delete();
      }

      if(!decisionInstanceIds.isEmpty()) {
        deleteHistoricDecisionInstanceByIds(decisionInstanceIds);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected List<HistoricDecisionInstanceEntity> findHistoricDecisionInstancesByDecisionDefinitionId(String decisionDefinitionId) {
    return getDbEntityManager().selectList("selectHistoricDecisionInstancesByDecisionDefinitionId", configureParameterizedQuery(decisionDefinitionId));
  }

  public void deleteHistoricDecisionInstanceByIds(List<String> decisionInstanceIds) {
    getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteHistoricDecisionInputInstanceByteArraysByDecisionInstanceIds", decisionInstanceIds);
    getDbEntityManager().deletePreserveOrder(ByteArrayEntity.class, "deleteHistoricDecisionOutputInstanceByteArraysByDecisionInstanceIds", decisionInstanceIds);
    getDbEntityManager().deletePreserveOrder(HistoricDecisionInputInstanceEntity.class, "deleteHistoricDecisionInputInstanceByDecisionInstanceIds", decisionInstanceIds);
    getDbEntityManager().deletePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "deleteHistoricDecisionOutputInstanceByDecisionInstanceIds", decisionInstanceIds);
    getDbEntityManager().deletePreserveOrder(HistoricDecisionInstanceEntity.class, "deleteHistoricDecisionInstanceByIds", decisionInstanceIds);
  }

  public void insertHistoricDecisionInstances(HistoricDecisionEvaluationEvent event) {
    if (isHistoryEnabled()) {

      HistoricDecisionInstanceEntity rootHistoricDecisionInstance = event.getRootHistoricDecisionInstance();
      insertHistoricDecisionInstance(rootHistoricDecisionInstance);

      for (HistoricDecisionInstanceEntity requiredHistoricDecisionInstances : event.getRequiredHistoricDecisionInstances()) {
        requiredHistoricDecisionInstances.setRootDecisionInstanceId(rootHistoricDecisionInstance.getId());
        insertHistoricDecisionInstance(requiredHistoricDecisionInstances);
      }
    }
  }

  protected void insertHistoricDecisionInstance(HistoricDecisionInstanceEntity historicDecisionInstance) {
    getDbEntityManager().insert(historicDecisionInstance);

    insertHistoricDecisionInputInstances(historicDecisionInstance.getInputs(), historicDecisionInstance.getId());
    insertHistoricDecisionOutputInstances(historicDecisionInstance.getOutputs(), historicDecisionInstance.getId());
  }

  protected void insertHistoricDecisionInputInstances(List<HistoricDecisionInputInstance> inputs, String decisionInstanceId) {
    for (HistoricDecisionInputInstance input : inputs) {
      HistoricDecisionInputInstanceEntity inputEntity = (HistoricDecisionInputInstanceEntity) input;
      inputEntity.setDecisionInstanceId(decisionInstanceId);

      getDbEntityManager().insert(inputEntity);
    }
  }

  protected void insertHistoricDecisionOutputInstances(List<HistoricDecisionOutputInstance> outputs, String decisionInstanceId) {
    for (HistoricDecisionOutputInstance output : outputs) {
      HistoricDecisionOutputInstanceEntity outputEntity = (HistoricDecisionOutputInstanceEntity) output;
      outputEntity.setDecisionInstanceId(decisionInstanceId);

      getDbEntityManager().insert(outputEntity);
    }
  }

  public List<HistoricDecisionInstance> findHistoricDecisionInstancesByQueryCriteria(HistoricDecisionInstanceQueryImpl query, Page page) {
    if (isHistoryEnabled()) {
      configureQuery(query);

      @SuppressWarnings("unchecked")
      List<HistoricDecisionInstance> decisionInstances = getDbEntityManager().selectList("selectHistoricDecisionInstancesByQueryCriteria", query, page);

      enrichHistoricDecisionsWithInputsAndOutputs(query, decisionInstances);

      return decisionInstances;
    } else {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  public List<ImmutablePair<String, String>> findHistoricDecisionInstanceDeploymentIdMappingsByQueryCriteria(HistoricDecisionInstanceQueryImpl query) {
    if (isHistoryEnabled()) {
      configureQuery(query);
      return getDbEntityManager().selectList("selectHistoricDecisionInstanceDeploymentIdMappingsByQueryCriteria", query);
    } else {
      return Collections.emptyList();
    }
  }



  public void enrichHistoricDecisionsWithInputsAndOutputs(HistoricDecisionInstanceQueryImpl query, List<HistoricDecisionInstance> decisionInstances) {
    Map<String, HistoricDecisionInstanceEntity> decisionInstancesById =
      new HashMap<>();
    for(HistoricDecisionInstance decisionInstance : decisionInstances) {
      decisionInstancesById.put(decisionInstance.getId(), (HistoricDecisionInstanceEntity) decisionInstance);
    }

    if (!decisionInstances.isEmpty() && query.isIncludeInput()) {
      appendHistoricDecisionInputInstances(decisionInstancesById, query);
    }

    if(!decisionInstances.isEmpty() && query.isIncludeOutputs()) {
      appendHistoricDecisionOutputInstances(decisionInstancesById, query);
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> findHistoricDecisionInstanceIdsForCleanup(Integer batchSize, int minuteFrom, int minuteTo) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("currentTimestamp", ClockUtil.getCurrentTime());
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    ListQueryParameterObject parameterObject = new ListQueryParameterObject(parameters, 0, batchSize);
    return (List<String>) getDbEntityManager().selectList("selectHistoricDecisionInstanceIdsForCleanup", parameterObject);
  }

  protected void appendHistoricDecisionInputInstances(Map<String, HistoricDecisionInstanceEntity> decisionInstancesById, HistoricDecisionInstanceQueryImpl query) {
    List<HistoricDecisionInputInstanceEntity> decisionInputInstances = findHistoricDecisionInputInstancesByDecisionInstanceIds(decisionInstancesById.keySet());
    initializeInputInstances(decisionInstancesById.values());

    for (HistoricDecisionInputInstanceEntity decisionInputInstance : decisionInputInstances) {

      HistoricDecisionInstanceEntity historicDecisionInstance = decisionInstancesById.get(decisionInputInstance.getDecisionInstanceId());
      historicDecisionInstance.addInput(decisionInputInstance);

      // do not fetch values for byte arrays eagerly (unless requested by the user)
      if (!isBinaryValue(decisionInputInstance) || query.isByteArrayFetchingEnabled()) {
        fetchVariableValue(decisionInputInstance, query.isCustomObjectDeserializationEnabled());
      }
    }
  }

  protected void initializeInputInstances(Collection<HistoricDecisionInstanceEntity> decisionInstances) {
    for (HistoricDecisionInstanceEntity decisionInstance : decisionInstances) {
      decisionInstance.setInputs(new ArrayList<HistoricDecisionInputInstance>());
    }
  }

  @SuppressWarnings("unchecked")
  protected List<HistoricDecisionInputInstanceEntity> findHistoricDecisionInputInstancesByDecisionInstanceIds(Set<String> historicDecisionInstanceKeys) {
    return getDbEntityManager().selectList("selectHistoricDecisionInputInstancesByDecisionInstanceIds", historicDecisionInstanceKeys);
  }

  protected boolean isBinaryValue(HistoricDecisionInputInstance decisionInputInstance) {
    return AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(decisionInputInstance.getTypeName());
  }

  protected void fetchVariableValue(HistoricDecisionInputInstanceEntity decisionInputInstance, boolean isCustomObjectDeserializationEnabled) {
    try {
      decisionInputInstance.getTypedValue(isCustomObjectDeserializationEnabled);
    } catch(Exception t) {
      // do not fail if one of the variables fails to load
      LOG.failedTofetchVariableValue(t);
    }
  }


  protected void appendHistoricDecisionOutputInstances(Map<String, HistoricDecisionInstanceEntity> decisionInstancesById, HistoricDecisionInstanceQueryImpl query) {
    List<HistoricDecisionOutputInstanceEntity> decisionOutputInstances = findHistoricDecisionOutputInstancesByDecisionInstanceIds(decisionInstancesById.keySet());
    initializeOutputInstances(decisionInstancesById.values());

    for (HistoricDecisionOutputInstanceEntity decisionOutputInstance : decisionOutputInstances) {

      HistoricDecisionInstanceEntity historicDecisionInstance = decisionInstancesById.get(decisionOutputInstance.getDecisionInstanceId());
      historicDecisionInstance.addOutput(decisionOutputInstance);

      // do not fetch values for byte arrays eagerly (unless requested by the user)
      if(!isBinaryValue(decisionOutputInstance) || query.isByteArrayFetchingEnabled()) {
        fetchVariableValue(decisionOutputInstance, query.isCustomObjectDeserializationEnabled());
      }
    }
  }

  protected void initializeOutputInstances(Collection<HistoricDecisionInstanceEntity> decisionInstances) {
    for (HistoricDecisionInstanceEntity decisionInstance : decisionInstances) {
      decisionInstance.setOutputs(new ArrayList<HistoricDecisionOutputInstance>());
    }
  }

  @SuppressWarnings("unchecked")
  protected List<HistoricDecisionOutputInstanceEntity> findHistoricDecisionOutputInstancesByDecisionInstanceIds(Set<String> decisionInstanceKeys) {
    return getDbEntityManager().selectList("selectHistoricDecisionOutputInstancesByDecisionInstanceIds", decisionInstanceKeys);
  }

  protected boolean isBinaryValue(HistoricDecisionOutputInstance decisionOutputInstance) {
    return AbstractTypedValueSerializer.BINARY_VALUE_TYPES.contains(decisionOutputInstance.getTypeName());
  }

  protected void fetchVariableValue(HistoricDecisionOutputInstanceEntity decisionOutputInstance, boolean isCustomObjectDeserializationEnabled) {
    try {
      decisionOutputInstance.getTypedValue(isCustomObjectDeserializationEnabled);
    } catch(Exception t) {
      // do not fail if one of the variables fails to load
      LOG.failedTofetchVariableValue(t);
    }
  }

  public HistoricDecisionInstanceEntity findHistoricDecisionInstance(String historicDecisionInstanceId) {
    if (isHistoryEnabled()) {
      return (HistoricDecisionInstanceEntity) getDbEntityManager().selectOne(
          "selectHistoricDecisionInstanceByDecisionInstanceId", configureParameterizedQuery(historicDecisionInstanceId));
    }
    return null;
  }

  public long findHistoricDecisionInstanceCountByQueryCriteria(HistoricDecisionInstanceQueryImpl query) {
    if (isHistoryEnabled()) {
      configureQuery(query);
      return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByQueryCriteria", query);
    } else {
      return 0;
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInstance> findHistoricDecisionInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectHistoricDecisionInstancesByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricDecisionInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectHistoricDecisionInstanceCountByNativeQuery", parameterMap);
  }

  protected void configureQuery(HistoricDecisionInstanceQueryImpl query) {
    getAuthorizationManager().configureHistoricDecisionInstanceQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

  @SuppressWarnings("unchecked")
  public List<CleanableHistoricDecisionInstanceReportResult> findCleanableHistoricDecisionInstancesReportByCriteria( CleanableHistoricDecisionInstanceReportImpl query, Page page) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());
    getAuthorizationManager().configureQueryHistoricFinishedInstanceReport(query, Resources.DECISION_DEFINITION);
    getTenantManager().configureQuery(query);
    return getDbEntityManager().selectList("selectFinishedDecisionInstancesReportEntities", query, page);
  }

  public long findCleanableHistoricDecisionInstancesReportCountByCriteria(CleanableHistoricDecisionInstanceReportImpl query) {
    query.setCurrentTimestamp(ClockUtil.getCurrentTime());
    getAuthorizationManager().configureQueryHistoricFinishedInstanceReport(query, Resources.DECISION_DEFINITION);
    getTenantManager().configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectFinishedDecisionInstancesReportEntitiesCount", query);
  }

  public void addRemovalTimeToDecisionsByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInstanceEntity.class, "updateHistoricDecisionInstancesByRootProcessInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInputInstanceEntity.class, "updateHistoricDecisionInputInstancesByRootProcessInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "updateHistoricDecisionOutputInstancesByRootProcessInstanceId", parameters);
  }

  public void addRemovalTimeToDecisionsByProcessInstanceId(String processInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInstanceEntity.class, "updateHistoricDecisionInstancesByProcessInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInputInstanceEntity.class, "updateHistoricDecisionInputInstancesByProcessInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "updateHistoricDecisionOutputInstancesByProcessInstanceId", parameters);
  }

  public void addRemovalTimeToDecisionsByRootDecisionInstanceId(String rootInstanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootDecisionInstanceId", rootInstanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInstanceEntity.class, "updateHistoricDecisionInstancesByRootDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInputInstanceEntity.class, "updateHistoricDecisionInputInstancesByRootDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "updateHistoricDecisionOutputInstancesByRootDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class,
          "updateDecisionInputByteArraysByRootDecisionInstanceId", parameters);
    getDbEntityManager()
        .updatePreserveOrder(ByteArrayEntity.class,
            "updateDecisionOutputByteArraysByRootDecisionInstanceId", parameters);
  }

  public void addRemovalTimeToDecisionsByDecisionInstanceId(String instanceId, Date removalTime) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("decisionInstanceId", instanceId);
    parameters.put("removalTime", removalTime);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInstanceEntity.class, "updateHistoricDecisionInstancesByDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionInputInstanceEntity.class, "updateHistoricDecisionInputInstancesByDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "updateHistoricDecisionOutputInstancesByDecisionInstanceId", parameters);

    getDbEntityManager()
      .updatePreserveOrder(ByteArrayEntity.class,
          "updateDecisionInputByteArraysByDecisionInstanceId", parameters);

    getDbEntityManager()
        .updatePreserveOrder(ByteArrayEntity.class,
            "updateDecisionOutputByteArraysByDecisionInstanceId", parameters);
  }

  public Map<Class<? extends DbEntity>, DbOperation> deleteHistoricDecisionsByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    Map<Class<? extends DbEntity>, DbOperation> deleteOperations = new HashMap<>();

    DbOperation deleteDecisionInputInstances = getDbEntityManager()
      .deletePreserveOrder(HistoricDecisionInputInstanceEntity.class, "deleteHistoricDecisionInputInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));

    deleteOperations.put(HistoricDecisionInputInstanceEntity.class, deleteDecisionInputInstances);

    DbOperation deleteDecisionOutputInstances = getDbEntityManager()
      .deletePreserveOrder(HistoricDecisionOutputInstanceEntity.class, "deleteHistoricDecisionOutputInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));

    deleteOperations.put(HistoricDecisionOutputInstanceEntity.class, deleteDecisionOutputInstances);

    DbOperation deleteDecisionInstances = getDbEntityManager()
      .deletePreserveOrder(HistoricDecisionInstanceEntity.class, "deleteHistoricDecisionInstancesByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));

    deleteOperations.put(HistoricDecisionInstanceEntity.class, deleteDecisionInstances);

    return deleteOperations;
  }

}
