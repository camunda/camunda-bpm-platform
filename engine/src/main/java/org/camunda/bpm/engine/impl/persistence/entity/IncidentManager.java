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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.IncidentQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author roman.smirnov
 */
public class IncidentManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<IncidentEntity> findIncidentsByExecution(String id) {
    return getDbEntityManager().selectList("selectIncidentsByExecutionId", id);
  }

  @SuppressWarnings("unchecked")
  public List<IncidentEntity> findIncidentsByProcessInstance(String id) {
    return getDbEntityManager().selectList("selectIncidentsByProcessInstanceId", id);
  }

  public long findIncidentCountByQueryCriteria(IncidentQueryImpl incidentQuery) {
    configureQuery(incidentQuery);
    return (Long) getDbEntityManager().selectOne("selectIncidentCountByQueryCriteria", incidentQuery);
  }

  public Incident findIncidentById(String id) {
    return (Incident) getDbEntityManager().selectById(IncidentEntity.class, id);
  }

  public List<Incident> findIncidentByConfiguration(String configuration) {
    return findIncidentByConfigurationAndIncidentType(configuration, null);
  }

  @SuppressWarnings("unchecked")
  public List<Incident> findIncidentByConfigurationAndIncidentType(String configuration, String incidentType) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("configuration", configuration);
    params.put("incidentType", incidentType);
    return getDbEntityManager().selectList("selectIncidentsByConfiguration", params);
  }

  @SuppressWarnings("unchecked")
  public List<Incident> findIncidentByQueryCriteria(IncidentQueryImpl incidentQuery, Page page) {
    configureQuery(incidentQuery);
    return getDbEntityManager().selectList("selectIncidentByQueryCriteria", incidentQuery, page);
  }

  protected void configureQuery(IncidentQueryImpl query) {
    getAuthorizationManager().configureIncidentQuery(query);
    getTenantManager().configureQuery(query);
  }

}
