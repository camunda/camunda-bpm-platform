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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.JobDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 * <p>Manager implementation for {@link JobDefinitionEntity}</p>
 *
 * @author Daniel Meyer
 *
 */
public class JobDefinitionManager extends AbstractManager {

  public JobDefinitionEntity findById(String jobDefinitionId) {
    return getDbEntityManager().selectById(JobDefinitionEntity.class, jobDefinitionId);
  }

  @SuppressWarnings("unchecked")
  public List<JobDefinitionEntity> findByProcessDefinitionId(String processDefinitionId) {
    return getDbEntityManager().selectList("selectJobDefinitionsByProcessDefinitionId", processDefinitionId);
  }

  public void deleteJobDefinitionsByProcessDefinitionId(String id) {
    getDbEntityManager().delete(JobDefinitionEntity.class, "deleteJobDefinitionsByProcessDefinitionId", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobDefinition> findJobDefnitionByQueryCriteria(JobDefinitionQueryImpl jobDefinitionQuery, Page page) {
    configureQuery(jobDefinitionQuery);
    return getDbEntityManager().selectList("selectJobDefinitionByQueryCriteria", jobDefinitionQuery, page);
  }

  public long findJobDefinitionCountByQueryCriteria(JobDefinitionQueryImpl jobDefinitionQuery) {
    configureQuery(jobDefinitionQuery);
    return (Long) getDbEntityManager().selectOne("selectJobDefinitionCountByQueryCriteria", jobDefinitionQuery);
  }

  public void updateJobDefinitionSuspensionStateById(String jobDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobDefinitionSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobDefinitionSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", false);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobDefinitionSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String processDefinitionTenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("isProcessDefinitionTenantIdSet", true);
    parameters.put("processDefinitionTenantId", processDefinitionTenantId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbEntityManager().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  protected void configureQuery(JobDefinitionQueryImpl query) {
    getAuthorizationManager().configureJobDefinitionQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

}
