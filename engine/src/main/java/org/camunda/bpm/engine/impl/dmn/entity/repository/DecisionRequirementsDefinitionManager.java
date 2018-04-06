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

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Johannes Heinemann
 */
public class DecisionRequirementsDefinitionManager extends AbstractManager implements AbstractResourceDefinitionManager<DecisionRequirementsDefinitionEntity> {

  public void insertDecisionRequirementsDefinition(DecisionRequirementsDefinitionEntity decisionRequirementsDefinition) {
    getDbEntityManager().insert(decisionRequirementsDefinition);
    createDefaultAuthorizations(decisionRequirementsDefinition);
  }

  public void deleteDecisionRequirementsDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(DecisionDefinitionEntity.class, "deleteDecisionRequirementsDefinitionsByDeploymentId", deploymentId);
  }

  public DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionById(String decisionRequirementsDefinitionId) {
    return getDbEntityManager().selectById(DecisionRequirementsDefinitionEntity.class, decisionRequirementsDefinitionId);
  }

  public String findPreviousDecisionRequirementsDefinitionId(String decisionRequirementsDefinitionKey, Integer version, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("key", decisionRequirementsDefinitionKey);
    params.put("version", version);
    params.put("tenantId", tenantId);
    return (String) getDbEntityManager().selectOne("selectPreviousDecisionRequirementsDefinitionId", params);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionRequirementsDefinition> findDecisionRequirementsDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectDecisionRequirementsDefinitionByDeploymentId", deploymentId);
  }

  public DecisionRequirementsDefinitionEntity findDecisionRequirementsDefinitionByDeploymentAndKey(String deploymentId, String decisionRequirementsDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    return (DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectDecisionRequirementsDefinitionByDeploymentAndKey", parameters);
  }

  /**
   * @return the latest version of the decision requirements definition with the given key and tenant id
   */
  public DecisionRequirementsDefinitionEntity findLatestDecisionRequirementsDefinitionByKeyAndTenantId(String decisionRequirementsDefinitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("decisionRequirementsDefinitionKey", decisionRequirementsDefinitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionRequirementsDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (DecisionRequirementsDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionRequirementsDefinitionByKeyAndTenantId", parameters);
    }
  }

  @SuppressWarnings("unchecked")
  public List<DecisionRequirementsDefinition> findDecisionRequirementsDefinitionsByQueryCriteria(DecisionRequirementsDefinitionQueryImpl query, Page page) {
    configureDecisionRequirementsDefinitionQuery(query);
    return getDbEntityManager().selectList("selectDecisionRequirementsDefinitionsByQueryCriteria", query, page);
  }

  public long findDecisionRequirementsDefinitionCountByQueryCriteria(DecisionRequirementsDefinitionQueryImpl query) {
    configureDecisionRequirementsDefinitionQuery(query);
    return (Long) getDbEntityManager().selectOne("selectDecisionRequirementsDefinitionCountByQueryCriteria", query);
  }

  protected void createDefaultAuthorizations(DecisionRequirementsDefinition decisionRequirementsDefinition) {
    if (isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newDecisionRequirementsDefinition(decisionRequirementsDefinition);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureDecisionRequirementsDefinitionQuery(DecisionRequirementsDefinitionQueryImpl query) {
    getAuthorizationManager().configureDecisionRequirementsDefinitionQuery(query);
    getTenantManager().configureQuery(query);
  }


  @Override
  public DecisionRequirementsDefinitionEntity findLatestDefinitionByKey(String key) {
    return null;
  }

  @Override
  public DecisionRequirementsDefinitionEntity findLatestDefinitionById(String id) {
    return getDbEntityManager().selectById(DecisionRequirementsDefinitionEntity.class, id);
  }

  @Override
  public DecisionRequirementsDefinitionEntity findLatestDefinitionByKeyAndTenantId(String definitionKey, String tenantId) {
    return null;
  }

  @Override
  public DecisionRequirementsDefinitionEntity findDefinitionByKeyVersionAndTenantId(String definitionKey, Integer definitionVersion, String tenantId) {
    return null;
  }

  @Override
  public DecisionRequirementsDefinitionEntity findDefinitionByKeyVersionTagAndTenantId(String definitionKey, String definitionVersionTag, String tenantId) {
    return null;
  }

  @Override
  public DecisionRequirementsDefinitionEntity findDefinitionByDeploymentAndKey(String deploymentId, String definitionKey) {
    return null;
  }

  @Override
  public DecisionRequirementsDefinitionEntity getCachedResourceDefinitionEntity(String definitionId) {
    return getDbEntityManager().getCachedEntity(DecisionRequirementsDefinitionEntity.class, definitionId);
  }
}
