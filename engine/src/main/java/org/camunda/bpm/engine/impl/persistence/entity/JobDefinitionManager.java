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

import org.camunda.bpm.engine.impl.JobDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.Page;
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
    return getDbSqlSession().selectById(JobDefinitionEntity.class, jobDefinitionId);
  }

  @SuppressWarnings("unchecked")
  public List<JobDefinitionEntity> findByProcessDefinitionId(String processDefinitionId) {
    return getDbSqlSession().selectList("selectJobDefinitionsByProcessDefinitionId", processDefinitionId);
  }

  public void deleteJobDefinitionsByProcessDefinitionId(String id) {
    getDbSqlSession().delete(JobDefinitionEntity.class, "deleteJobDefinitionsByProcessDefinitionId", id);
  }

  @SuppressWarnings("unchecked")
  public List<JobDefinition> findJobDefnitionByQueryCriteria(JobDefinitionQueryImpl jobDefinitionQuery, Page page) {
    return getDbSqlSession().selectList("selectJobDefinitionByQueryCriteria", jobDefinitionQuery, page);
  }

  public long findJobDefinitionCountByQueryCriteria(JobDefinitionQueryImpl jobDefinitionQuery) {
    return (Long) getDbSqlSession().selectOne("selectJobDefinitionCountByQueryCriteria", jobDefinitionQuery);
  }

  public void updateJobDefinitionSuspensionStateById(String jobDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("jobDefinitionId", jobDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", parameters);
  }

  public void updateJobDefinitionSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionId", processDefinitionId);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", parameters);
  }

  public void updateJobDefinitionSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("processDefinitionKey", processDefinitionKey);
    parameters.put("suspensionState", suspensionState.getStateCode());
    getDbSqlSession().update(JobDefinitionEntity.class, "updateJobDefinitionSuspensionStateByParameters", parameters);
  }

}
