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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class DecisionDefinitionManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public void insertDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    getDbEntityManager().insert(decisionDefinition);
    createDefaultAuthorizations(decisionDefinition);
  }

  public void deleteDecisionDefinitionsByDeploymentId(String deploymentId) {
    getDbEntityManager().delete(DecisionDefinitionEntity.class, "deleteDecisionDefinitionsByDeploymentId", deploymentId);
  }

  public DecisionDefinitionEntity findDecisionDefinitionById(String decisionDefinitionId) {
    return getDbEntityManager().selectById(DecisionDefinitionEntity.class, decisionDefinitionId);
  }

  /**
   * @return the latest version of the decision definition with the given key (from any tenant)
   *
   * @throws ProcessEngineException if more than one tenant has a decision definition with the given key
   *
   * @see #findLatestDecisionDefinitionByKeyAndTenantId(String, String)
   */
  public DecisionDefinitionEntity findLatestDecisionDefinitionByKey(String decisionDefinitionKey) {
    @SuppressWarnings("unchecked")
    List<DecisionDefinitionEntity> decisionDefinitions = getDbEntityManager().selectList("selectLatestDecisionDefinitionByKey", decisionDefinitionKey);

    if (decisionDefinitions.isEmpty()) {
      return null;

    } else if (decisionDefinitions.size() == 1) {
      return decisionDefinitions.iterator().next();

    } else {
      throw LOG.multipleTenantsForDecisionDefinitionKeyException(decisionDefinitionKey);
    }
  }

  /**
   * @return the latest version of the decision definition with the given key and tenant id
   *
   * @see #findLatestDecisionDefinitionByKeyAndTenantId(String, String)
   */
  public DecisionDefinitionEntity findLatestDecisionDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("decisionDefinitionKey", decisionDefinitionKey);
    parameters.put("tenantId", tenantId);

    if (tenantId == null) {
      return (DecisionDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionDefinitionByKeyWithoutTenantId", parameters);
    } else {
      return (DecisionDefinitionEntity) getDbEntityManager().selectOne("selectLatestDecisionDefinitionByKeyAndTenantId", parameters);
    }
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
    configureDecisionDefinitionQuery(decisionDefinitionQuery);
    return getDbEntityManager().selectList("selectDecisionDefinitionsByQueryCriteria", decisionDefinitionQuery, page);
  }

  public long findDecisionDefinitionCountByQueryCriteria(DecisionDefinitionQueryImpl decisionDefinitionQuery) {
    configureDecisionDefinitionQuery(decisionDefinitionQuery);
    return (Long) getDbEntityManager().selectOne("selectDecisionDefinitionCountByQueryCriteria", decisionDefinitionQuery);
  }

  public String findPreviousDecisionDefinitionId(String decisionDefinitionKey, Integer version, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("key", decisionDefinitionKey);
    params.put("version", version);
    params.put("tenantId", tenantId);
    return (String) getDbEntityManager().selectOne("selectPreviousDecisionDefinitionId", params);
  }

  @SuppressWarnings("unchecked")
  public List<DecisionDefinition> findDecisionDefinitionByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectDecisionDefinitionByDeploymentId", deploymentId);
  }

  protected void createDefaultAuthorizations(DecisionDefinition decisionDefinition) {
    if(isAuthorizationEnabled()) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();
      AuthorizationEntity[] authorizations = provider.newDecisionDefinition(decisionDefinition);
      saveDefaultAuthorizations(authorizations);
    }
  }

  protected void configureDecisionDefinitionQuery(DecisionDefinitionQueryImpl query) {
    getAuthorizationManager().configureDecisionDefinitionQuery(query);
  }

}
