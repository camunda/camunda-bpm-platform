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
package org.camunda.bpm.engine.impl.cmmn.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.repository.CaseDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionManager extends AbstractManager {

  public void insertCaseDefinition(CaseDefinitionEntity caseDefinition) {
    getDbEntityManager().insert(caseDefinition);
  }

  public void deleteCaseDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(CaseDefinitionEntity.class, "deleteCaseDefinitionsByDeploymentId", deploymentId);
  }

  public CaseDefinitionEntity findCaseDefinitionById(String caseDefinitionId) {
    return getDbEntityManager().selectById(CaseDefinitionEntity.class, caseDefinitionId);
  }

  public CaseDefinitionEntity findLatestCaseDefinitionByKey(String caseDefinitionKey) {
    return (CaseDefinitionEntity) getDbEntityManager().selectOne("selectLatestCaseDefinitionByKey", caseDefinitionKey);
  }

  public CaseDefinitionEntity findCaseDefinitionByKeyAndVersion(String caseDefinitionKey, Integer caseDefinitionVersion) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("caseDefinitionVersion", caseDefinitionVersion);
    parameters.put("caseDefinitionKey", caseDefinitionKey);
    return (CaseDefinitionEntity) getDbEntityManager().selectOne("selectCaseDefinitionByKeyAndVersion", parameters);
  }

  public CaseDefinitionEntity findCaseDefinitionByDeploymentAndKey(String deploymentId, String caseDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("caseDefinitionKey", caseDefinitionKey);
    return (CaseDefinitionEntity) getDbEntityManager().selectOne("selectCaseDefinitionByDeploymentAndKey", parameters);
  }

  public String findPreviousCaseDefinitionIdByKeyAndVersion(String caseDefinitionKey, Integer version) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("key", caseDefinitionKey);
    params.put("version", version);
    return (String) getDbEntityManager().selectOne("selectPreviousCaseDefinitionIdByKeyAndVersion", params);
  }

  @SuppressWarnings("unchecked")
  public List<CaseDefinition> findCaseDefinitionsByQueryCriteria(CaseDefinitionQueryImpl caseDefinitionQuery, Page page) {
    return getDbEntityManager().selectList("selectCaseDefinitionsByQueryCriteria", caseDefinitionQuery, page);
  }

  public long findCaseDefinitionCountByQueryCriteria(CaseDefinitionQueryImpl caseDefinitionQuery) {
    return (Long) getDbEntityManager().selectOne("selectCaseDefinitionCountByQueryCriteria", caseDefinitionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<CaseDefinition> findCaseDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectCaseDefinitionByDeploymentId", deploymentId);
  }
}
