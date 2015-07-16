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
package org.camunda.bpm.engine.impl.dmn.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class DecisionDefinitionManager extends AbstractManager {

  public void insertDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    getDbEntityManager().insert(decisionDefinition);
  }

  public void deleteDecisionDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(DecisionDefinitionEntity.class, "deleteDecisionDefinitionsByDeploymentId", deploymentId);
  }

  public DecisionDefinitionEntity findDecisionDefinitionById(String decisionDefinitionId) {
    return getDbEntityManager().selectById(DecisionDefinitionEntity.class, decisionDefinitionId);
  }

  public DecisionDefinitionEntity findLatestDecisionDefinitionByKey(String decisionDefinitionKey) {
    return (DecisionDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionDefinitionByKey", decisionDefinitionKey);
  }

  public DecisionDefinitionEntity findDecisionDefinitionByKeyAndVersion(String decisionDefinitionKey, Integer decisionDefinitionVersion) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("decisionDefinitionVersion", decisionDefinitionVersion);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    return (DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByKeyAndVersion", parameters);
  }

  public DecisionDefinitionEntity findDecisionDefinitionByDeploymentAndKey(String deploymentId, String decisionDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    return (DecisionDefinitionEntity) getDbEntityManager().selectOne("selectDecisionDefinitionByDeploymentAndKey", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionDefinition> findDecisionDefinitionsByQueryCriteria(DecisionDefinitionQueryImpl decisionDefinitionQuery, Page page) {
    return getDbEntityManager().selectList("selectDecisionDefinitionsByQueryCriteria", decisionDefinitionQuery, page);
  }

  public long findDecisionDefinitionCountByQueryCriteria(DecisionDefinitionQueryImpl decisionDefinitionQuery) {
    return (Long) getDbEntityManager().selectOne("selectDecisionDefinitionCountByQueryCriteria", decisionDefinitionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionDefinition> findDecisionDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectDecisionDefinitionByDeploymentId", deploymentId);
  }

}
